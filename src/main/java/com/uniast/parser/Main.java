package com.uniast.parser;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import com.uniast.parser.resolver.MavenDependencyResolver;
import com.uniast.parser.resolver.DependencyResult;
import com.uniast.parser.parser.IJavaParser;
import com.uniast.parser.parser.JavaParserFactory;
import com.uniast.parser.input.RepoInput;
import com.uniast.parser.config.ParseOptions;
import com.uniast.parser.model.Repository;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

/**
 * Main CLI entry point for reni-java-parser
 */
@Command(name = "java-parser", mixinStandardHelpOptions = true, version = "1.0.0",
        description = "Java parser for reni using Eclipse JDT.Core - Generates UniAST-compliant JSON")
public class Main implements Callable<Integer> {

    @Parameters(index = "0", description = "Repository path to parse", arity = "1")
    private String repoPath;

    @Option(names = {"-o", "--output"}, description = "Output JSON file path (for single module). For Monorepo, outputs to ~/.asts/)")
    private String outputPath;

    @Option(names = {"--no-bindings"}, description = "Disable type binding resolution")
    private boolean noBindings;

    @Option(names = {"--verbose"}, description = "Enable verbose logging for classpath and parsing details")
    private boolean verbose;

    @Option(names = {"--include-stdlib"}, description = "Include Java standard library dependencies (java.*, javax.* etc.) in the output. By default, these are excluded to reduce noise.")
    private boolean includeStandardLibraries;

    @Option(names = {"--skip-maven"}, description = "Skip Maven dependency resolution. Use this when pom.xml cannot be parsed (e.g., missing parent POM in private repository). Module info will be extracted from pom.xml directly without full Maven model building.")
    private boolean skipMaven;

    @Option(names = {"-m", "--module"}, description = "Module name (groupId:artifactId:version) for manual override. If not provided, will try to extract from pom.xml.")
    private String moduleName;

    @Option(names = {"-v", "--version"}, description = "Module version for manual override (used with --module).")
    private String moduleVersion;

    @Option(names = {"-t", "--threads"}, description = "Number of parallel threads for Monorepo parsing. Default: 2")
    private int threads = 2;

    // AST output directory
    private static final String AST_DIR = System.getProperty("user.home") + "/.asts";

    @Override
    public Integer call() throws Exception {
        // Convert repoPath to absolute path
        repoPath = Paths.get(repoPath).toAbsolutePath().normalize().toString();

        // Ensure AST directory exists
        Path astDir = Paths.get(AST_DIR);
        if (!Files.exists(astDir)) {
            Files.createDirectories(astDir);
        }

        java.io.PrintStream originalErr = System.err;

        // Parse pom.xml to check if this is a Monorepo
        java.nio.file.Path pomFile = Paths.get(repoPath, "pom.xml");
        List<String> modules = new ArrayList<>();

        if (Files.exists(pomFile)) {
            MavenDependencyResolver resolver = new MavenDependencyResolver();
            modules = resolver.parseModules(pomFile);
        }

        if (!modules.isEmpty()) {
            // Monorepo: parse each module in parallel
            return parseMonorepo(modules, astDir, originalErr);
        } else {
            // Single module: parse normally
            return parseSingleModule(pomFile, astDir, originalErr);
        }
    }

    /**
     * Parse Monorepo: each module -> separate JSON file
     */
    private int parseMonorepo(List<String> modules, Path astDir, java.io.PrintStream originalErr) throws Exception {
        originalErr.println("📦 Detected Monorepo with " + modules.size() + " modules");
        originalErr.println("🧵 Using " + threads + " parallel threads");

        // Generate base filename from repo path
        String baseName = generateFileName(repoPath);

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        for (String module : modules) {
            final String moduleName = module;
            CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return parseModule(moduleName, baseName, astDir);
                } catch (Exception e) {
                    System.err.println("❌ Failed to parse module " + moduleName + ": " + e.getMessage());
                    return 1;
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all to complete
        int failed = 0;
        for (CompletableFuture<Integer> future : futures) {
            failed += future.join();
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        originalErr.println("✅ Monorepo parsing complete: " + (modules.size() - failed) + "/" + modules.size() + " succeeded");
        return failed > 0 ? 1 : 0;
    }

    /**
     * Parse a single module within Monorepo
     */
    private int parseModule(String moduleName, String baseName, Path astDir) throws Exception {
        String modulePath = repoPath + "/" + moduleName;
        java.nio.file.Path modulePomFile = Paths.get(modulePath, "pom.xml");

        // Extract module info
        String moduleId;
        String moduleVersion;

        if (Files.exists(modulePomFile)) {
            MavenDependencyResolver resolver = new MavenDependencyResolver();
            DependencyResult moduleInfo;

            if (skipMaven) {
                moduleInfo = resolver.extractModuleInfoSimple(modulePomFile);
            } else {
                try {
                    moduleInfo = resolver.extractModuleInfo(modulePomFile);
                } catch (Exception e) {
                    moduleInfo = resolver.extractModuleInfoSimple(modulePomFile);
                }
            }

            moduleId = moduleInfo.toModulePath();
            moduleVersion = moduleInfo.getVersion();
        } else {
            // Fallback: use module name as ID
            moduleId = moduleName;
            moduleVersion = "unknown";
        }

        // Create parser using factory
        RepoInput input = new RepoInput(Paths.get(modulePath), moduleId, moduleVersion);
        ParseOptions options = new ParseOptions();
        options.setResolveBindings(!noBindings);
        options.setVerbose(verbose);
        options.setExcludeStandardLibraries(!includeStandardLibraries);

        IJavaParser parser = JavaParserFactory.createParser(input, options);

        // Parse and serialize
        Repository repository = parser.parse(input, options);
        ObjectMapper objectMapper = createObjectMapper();
        String json = objectMapper.writeValueAsString(repository);

        // Generate output filename: ~/.asts/{baseName}-{moduleName}.json
        String fileName = baseName + "-" + moduleName + ".json";
        Path outputPath = astDir.resolve(fileName);
        Path tempPath = Paths.get(outputPath.toString() + ".tmp");

        // Write with atomic move
        Files.writeString(tempPath, json);
        Files.move(tempPath, outputPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        System.err.println("✅ Wrote: " + outputPath);
        return 0;
    }

    /**
     * Parse single module (original behavior)
     */
    private int parseSingleModule(java.nio.file.Path pomFile, Path astDir, java.io.PrintStream originalErr) throws Exception {
        MavenDependencyResolver resolver = new MavenDependencyResolver();
        DependencyResult moduleInfo;

        // Extract module info
        if (moduleName != null) {
            String[] parts = moduleName.split(":");
            if (parts.length >= 2) {
                String groupId = parts[0];
                String artifactId = parts[1];
                String version = (moduleVersion != null) ? moduleVersion : "unknown";
                moduleInfo = new DependencyResult(groupId, artifactId, version);
            } else {
                throw new IllegalArgumentException("Invalid module name format. Expected groupId:artifactId:version");
            }
        } else if (skipMaven || !Files.exists(pomFile)) {
            moduleInfo = resolver.extractModuleInfoSimple(pomFile);
        } else {
            try {
                moduleInfo = resolver.extractModuleInfo(pomFile);
            } catch (Exception e) {
                originalErr.println("⚠️  Maven 解析失败: " + e.getMessage());
                originalErr.println("⚠️  自动切换到简单模式");
                moduleInfo = resolver.extractModuleInfoSimple(pomFile);
            }
        }

        String moduleId = moduleInfo.toModulePath();
        originalErr.println("📦 " + moduleId);

        // Create parser using factory
        RepoInput input = new RepoInput(Paths.get(repoPath), moduleId, moduleInfo.getVersion());
        ParseOptions options = new ParseOptions();
        options.setResolveBindings(!noBindings);
        options.setVerbose(verbose);
        options.setExcludeStandardLibraries(!includeStandardLibraries);

        IJavaParser parser = JavaParserFactory.createParser(input, options);

        // Parse and serialize
        Repository repository = parser.parse(input, options);
        ObjectMapper objectMapper = createObjectMapper();
        String json = objectMapper.writeValueAsString(repository);

        // Determine output path
        Path outputFilePath;
        if (outputPath != null) {
            outputFilePath = Paths.get(outputPath);
        } else {
            // Default: ~/.asts/{hash}-{artifactId}.json
            String baseName = generateFileName(repoPath);
            String fileName = baseName + "-" + moduleInfo.getArtifactId() + ".json";
            outputFilePath = astDir.resolve(fileName);
        }

        // Ensure parent directory exists
        Files.createDirectories(outputFilePath.getParent());

        // Write with atomic move
        Path tempPath = Paths.get(outputFilePath.toString() + ".tmp");
        Files.writeString(tempPath, json);
        Files.move(tempPath, outputFilePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        originalErr.println("✅ Wrote: " + outputFilePath);
        return 0;
    }

    /**
     * Generate safe filename from absolute path
     * /Users/bytedance/astRepo/test -> Users-bytedance-astRepo-test
     */
    private String generateFileName(String absPath) {
        StringBuilder sb = new StringBuilder();
        for (char c : absPath.toCharArray()) {
            if (c == '/' || c == '\\') {
                sb.append("-");
            } else if (c == ':') {
                // Skip drive letter colon on Windows
            } else {
                sb.append(c);
            }
        }
        // Only remove trailing hyphens, keep leading hyphen (from root path /)
        String result = sb.toString();
        while (result.endsWith("-")) result = result.substring(0, result.length() - 1);
        return result;
    }

    /**
     * Create configured ObjectMapper for JSON serialization
     */
    private ObjectMapper createObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        om.disable(SerializationFeature.INDENT_OUTPUT);
        om.configure(MapperFeature.USE_ANNOTATIONS, true);
        om.configure(MapperFeature.AUTO_DETECT_FIELDS, false);
        om.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, false);
        return om;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }
}
