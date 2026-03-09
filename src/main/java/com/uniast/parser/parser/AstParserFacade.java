package com.uniast.parser.parser;

import com.uniast.parser.model.*;
import com.uniast.parser.input.RepoInput;
import com.uniast.parser.config.ParseOptions;
import com.uniast.parser.helper.JavaModelHelper;
import com.uniast.parser.builder.ReferenceGraphBuilder;
import com.uniast.parser.builder.TypeHierarchyBuilder;
import com.uniast.parser.resolver.MavenDependencyResolver;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.uniast.parser.model.UniType;
import com.uniast.parser.model.TypeKind;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AST 解析协调器
 * 实现 IJavaParser 接口，协调各组件完成解析任务
 */
public class AstParserFacade implements IJavaParser {

    private final JavaModelHelper modelHelper;
    private final JdtAstConverter converter;
    private final MethodBodyAnalyzer methodAnalyzer;
    private final ObjectMapper objectMapper;

    private String repoPath;
    private String moduleName;
    private String moduleVersion;
    private boolean resolveBindings;
    private boolean verbose;
    private boolean excludeStandardLibraries;

    public AstParserFacade() {
        this.modelHelper = new JavaModelHelper();
        this.converter = new JdtAstConverter(modelHelper, "", true, false, true);
        this.methodAnalyzer = new MethodBodyAnalyzer(converter, modelHelper, false, true);
        this.objectMapper = createObjectMapper();
    }

    public AstParserFacade(String moduleName, boolean resolveBindings, boolean verbose, boolean excludeStandardLibraries) {
        this.modelHelper = new JavaModelHelper();
        this.moduleName = moduleName;
        this.resolveBindings = resolveBindings;
        this.verbose = verbose;
        this.excludeStandardLibraries = excludeStandardLibraries;

        this.converter = new JdtAstConverter(modelHelper, moduleName, resolveBindings, verbose, excludeStandardLibraries);
        this.methodAnalyzer = new MethodBodyAnalyzer(converter, modelHelper, verbose, excludeStandardLibraries);
        this.objectMapper = createObjectMapper();
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.disable(SerializationFeature.INDENT_OUTPUT);
        om.configure(MapperFeature.USE_ANNOTATIONS, true);
        om.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
        om.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, false);
        return om;
    }

    @Override
    public Repository parse(RepoInput input, ParseOptions options) {
        this.repoPath = input.getRepoPath().toString();
        this.moduleName = input.getModuleName();
        this.moduleVersion = input.getModuleVersion();
        this.resolveBindings = options.isResolveBindings();
        this.verbose = options.isVerbose();
        this.excludeStandardLibraries = options.isExcludeStandardLibraries();

        // Recreate converter and analyzer with correct settings
        // 先创建带 null analyzer 的 conv，再创建 analyzer，最后创建带 analyzer 的 conv
        JdtAstConverter conv = new JdtAstConverter(modelHelper, moduleName, resolveBindings, verbose, excludeStandardLibraries, null);
        MethodBodyAnalyzer analyzer = new MethodBodyAnalyzer(conv, modelHelper, verbose, excludeStandardLibraries);
        // 重新创建 conv，这次传入 analyzer
        conv = new JdtAstConverter(modelHelper, moduleName, resolveBindings, verbose, excludeStandardLibraries, analyzer);

        Repository repo = new Repository();
        repo.setId(moduleName);
        repo.setPath(repoPath);

        UniModule uniModule = new UniModule();
        uniModule.setLanguage(Language.JAVA);
        uniModule.setName(moduleName);
        uniModule.setVersion(moduleVersion);
        uniModule.setDir(".");

        Path repoPathAbs = Paths.get(repoPath).toAbsolutePath();
        if (verbose) {
            System.err.println("📁 [Verbose] Absolute repository path: " + repoPathAbs);
        }

        List<Path> javaFiles;
        try {
            javaFiles = findJavaFiles(repoPathAbs);
        } catch (IOException e) {
            throw new RuntimeException("Failed to find Java files: " + e.getMessage(), e);
        }
        parseJavaFilesBatch(javaFiles, repoPathAbs.toString(), uniModule, conv, analyzer);

        // Post-processing
        associateMethodsToTypes(repo);
        associateInterfaceImplementationsV2(repo);
        associateSuperClassHierarchy(repo);

        repo.addModule(moduleName, uniModule);

        // Build reference graph
        ReferenceGraphBuilder graphBuilder = new ReferenceGraphBuilder(verbose);
        graphBuilder.build(repo);

        return repo;
    }

    /**
     * Legacy method for backward compatibility
     */
    public String parseRepository() throws IOException {
        Repository repo = parse(new RepoInput(Paths.get(repoPath), moduleName, moduleVersion),
                               createParseOptions());
        return objectMapper.writeValueAsString(repo);
    }

    private ParseOptions createParseOptions() {
        ParseOptions opts = new ParseOptions();
        opts.setResolveBindings(resolveBindings);
        opts.setVerbose(verbose);
        opts.setExcludeStandardLibraries(excludeStandardLibraries);
        return opts;
    }

    private List<Path> findJavaFiles(Path dir) throws IOException {
        return Files.walk(dir)
            .filter(p -> p.toString().endsWith(".java"))
            .collect(Collectors.toList());
    }

    private void parseJavaFilesBatch(List<Path> javaFiles, String repoBasePath, UniModule uniModule,
                                     JdtAstConverter conv, MethodBodyAnalyzer analyzer) {
        if (javaFiles.isEmpty()) return;

        try {
            List<String> classpathEntries = new ArrayList<>();
            Path repoPath = Paths.get(repoBasePath);
            Path pomFile = repoPath.resolve("pom.xml");

            // Add target/classes
            Path targetClasses = repoPath.resolve("target/classes");
            if (Files.exists(targetClasses)) {
                classpathEntries.add(targetClasses.toAbsolutePath().toString());
            }

            // Resolve Maven dependencies
            if (Files.exists(pomFile)) {
                try {
                    MavenDependencyResolver resolver = new MavenDependencyResolver();
                    List<String> dependencies = resolver.resolve(pomFile);
                    classpathEntries.addAll(dependencies);
                    if (verbose) {
                        System.err.println("✓ Resolved " + dependencies.size() + " Maven dependencies");
                    }
                } catch (Exception e) {
                    System.err.println("⚠️  Failed to resolve Maven dependencies: " + e.getMessage());
                }
            }

            List<String> sourcePaths = detectSourceDirectories(repoBasePath, javaFiles);

            String[] sourceFilePaths = javaFiles.stream()
                .map(p -> p.toAbsolutePath().toString())
                .toArray(String[]::new);
            String[] encodings = new String[javaFiles.size()];
            Arrays.fill(encodings, "UTF-8");

            ASTParser parser = ASTParser.newParser(AST.JLS21);
            parser.setEnvironment(
                classpathEntries.toArray(new String[0]),
                sourcePaths.toArray(new String[0]),
                null,
                true
            );
            parser.setResolveBindings(true);
            parser.setBindingsRecovery(true);
            parser.setCompilerOptions(JavaCore.getOptions());

            FileASTRequestor requestor = new FileASTRequestor() {
                @Override
                public void acceptAST(String sourceFilePath, CompilationUnit cu) {
                    try {
                        Path absolutePath = Paths.get(sourceFilePath);
                        String relativePath = Paths.get(repoBasePath).relativize(absolutePath).toString();
                        String sourceCode = Files.readString(absolutePath);

                        String pkgPath = "";
                        if (cu.getPackage() != null) {
                            pkgPath = cu.getPackage().getName().getFullyQualifiedName();
                        }

                        UniPackage pkg = uniModule.getPackages().get(pkgPath);
                        if (pkg == null) {
                            pkg = new UniPackage();
                            pkg.setPkgPath(pkgPath);
                            pkg.setTest(relativePath.contains("/test/") || relativePath.contains("\\test\\"));
                            uniModule.addPackage(pkgPath, pkg);
                        }

                        for (Object type : cu.types()) {
                            if (type instanceof AbstractTypeDeclaration) {
                                AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration) type;
                                com.uniast.parser.model.UniType uniType = conv.convertTypeDeclaration(
                                    typeDecl, pkgPath, moduleName, relativePath, sourceCode, cu, pkg
                                );
                                if (uniType != null) {
                                    pkg.addType(uniType.getName(), uniType);
                                }
                            }
                        }

                        // Add file entry
                        UniFile uniFileEntry = new UniFile(relativePath);
                        uniFileEntry.setModPath(moduleName);
                        uniFileEntry.setPkgPath(pkgPath);

                        for (Object type : cu.types()) {
                            if (type instanceof AbstractTypeDeclaration) {
                                AbstractTypeDeclaration typeDecl = (AbstractTypeDeclaration) type;
                                uniFileEntry.addTypeName(typeDecl.getName().getIdentifier());
                                if (typeDecl instanceof TypeDeclaration) {
                                    TypeDeclaration classDecl = (TypeDeclaration) typeDecl;
                                    for (MethodDeclaration method : classDecl.getMethods()) {
                                        uniFileEntry.addFunctionName(method.getName().getIdentifier());
                                    }
                                }
                            }
                        }

                        for (Object imp : cu.imports()) {
                            if (imp instanceof ImportDeclaration) {
                                ImportDeclaration importDecl = (ImportDeclaration) imp;
                                Import uniImport = new Import();
                                uniImport.setPath(importDecl.getName().getFullyQualifiedName());
                                uniFileEntry.addImport(uniImport);
                            }
                        }

                        uniModule.addFile(relativePath, uniFileEntry);

                    } catch (IOException e) {
                        System.err.println("Error processing file: " + sourceFilePath + " - " + e.getMessage());
                    }
                }
            };

            parser.createASTs(sourceFilePaths, encodings, new String[0], requestor, null);

        } catch (Exception e) {
            System.err.println("⚠️  Failed to parse Java files: " + e.getMessage());
        }
    }

    private List<String> detectSourceDirectories(String repoBasePath, List<Path> javaFiles) {
        Set<String> sourceDirs = new HashSet<>();
        for (Path javaFile : javaFiles) {
            Path parent = javaFile.getParent();
            while (parent != null && parent.toString().length() > repoBasePath.length()) {
                if (parent.getFileName() != null) {
                    String dirName = parent.getFileName().toString();
                    if (dirName.equals("java") || dirName.equals("src")) {
                        sourceDirs.add(parent.toString());
                        break;
                    }
                }
                parent = parent.getParent();
            }
        }
        return new ArrayList<>(sourceDirs);
    }

    private void associateMethodsToTypes(Repository repo) {
        for (UniModule mod : repo.getModules().values()) {
            for (UniPackage pkg : mod.getPackages().values()) {
                if (pkg.getFunctions() == null) continue;

                for (Function func : pkg.getFunctions().values()) {
                    if (func.getReceiver() != null && func.getReceiver().getType() != null) {
                        String typeName = func.getReceiver().getType().getName();
                        UniType type = pkg.getTypes() != null ? pkg.getTypes().get(typeName) : null;

                        if (type != null) {
                            Identity methodId = new Identity();
                            methodId.setModPath(func.getModPath());
                            methodId.setPkgPath(func.getPkgPath());
                            methodId.setName(func.getName());
                            type.addMethod(func.getName(), methodId);
                        }
                    }
                }
            }
        }
    }

    private void associateInterfaceImplementationsV2(Repository repo) {
        TypeHierarchyBuilder builder = new TypeHierarchyBuilder(modelHelper, verbose);
        builder.build(repo);
    }

    private void associateSuperClassHierarchy(Repository repo) {
        for (UniModule mod : repo.getModules().values()) {
            String dir = mod.getDir();
            if (dir != null && !dir.equals(".") && !dir.isEmpty()) continue;

            for (UniPackage pkg : mod.getPackages().values()) {
                if (pkg.getTypes() == null) continue;

                for (com.uniast.parser.model.UniType type : pkg.getTypes().values()) {
                    if (type.getInlineStruct() != null && !type.getInlineStruct().isEmpty()) {
                        for (Dependency superDep : type.getInlineStruct()) {
                            Identity superId = superDep.toIdentity();
                            com.uniast.parser.model.UniType superType = findType(repo, superId);
                            if (superType != null) {
                                Identity subId = new Identity();
                                subId.setModPath(type.getModPath());
                                subId.setPkgPath(type.getPkgPath());
                                subId.setName(type.getName());
                                superType.addImplementation(subId);
                            }
                        }
                    }
                }
            }
        }
    }

    private com.uniast.parser.model.UniType findType(Repository repo, Identity typeId) {
        for (UniModule mod : repo.getModules().values()) {
            if (!mod.getName().equals(typeId.getModPath())) continue;

            for (UniPackage pkg : mod.getPackages().values()) {
                if (!pkg.getPkgPath().equals(typeId.getPkgPath())) continue;

                if (pkg.getTypes() != null) {
                    return pkg.getTypes().get(typeId.getName());
                }
            }
        }
        return null;
    }
}
