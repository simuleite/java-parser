package com.uniast.parser.parser;

import com.uniast.parser.model.*;
import com.uniast.parser.helper.JavaModelHelper;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.List;

import com.uniast.parser.model.UniType;
import com.uniast.parser.model.TypeKind;

/**
 * JDT AST 转换器
 * 负责 Eclipse JDT AST 节点 → UniAST Model 转换
 */
public class JdtAstConverter {

    private final JavaModelHelper modelHelper;
    private final boolean resolveBindings;
    private final boolean verbose;
    private final String moduleName;
    private final boolean excludeStandardLibraries;
    private final MethodBodyAnalyzer methodBodyAnalyzer;

    public JdtAstConverter(JavaModelHelper modelHelper, String moduleName,
                          boolean resolveBindings, boolean verbose, boolean excludeStandardLibraries) {
        this(modelHelper, moduleName, resolveBindings, verbose, excludeStandardLibraries, null);
    }

    public JdtAstConverter(JavaModelHelper modelHelper, String moduleName,
                          boolean resolveBindings, boolean verbose, boolean excludeStandardLibraries,
                          MethodBodyAnalyzer methodBodyAnalyzer) {
        this.modelHelper = modelHelper;
        this.moduleName = moduleName;
        this.resolveBindings = resolveBindings;
        this.verbose = verbose;
        this.excludeStandardLibraries = excludeStandardLibraries;
        this.methodBodyAnalyzer = methodBodyAnalyzer;
    }

    /**
     * Convert TypeDeclaration to Type
     */
    public UniType convertTypeDeclaration(AbstractTypeDeclaration typeDecl, String pkgPath,
                                       String modPath, String filePath, String fullSource,
                                       CompilationUnit cu, UniPackage pkg) {
        UniType type = new UniType();

        // Set identity
        type.setModPath(modPath);
        type.setPkgPath(pkgPath);
        type.setName(typeDecl.getName().getIdentifier());

        // Set file location
        type.setFile(filePath);
        int startOffset = typeDecl.getStartPosition();
        int length = typeDecl.getLength();
        type.setStartOffset(startOffset);
        type.setEndOffset(startOffset + length);

        // Set line number
        int lineNumber = cu.getLineNumber(startOffset);
        if (lineNumber > 0) {
            type.setLine(lineNumber);
        }

        // Set end line number
        int endOffset = startOffset + length;
        int endLineNumber = cu.getLineNumber(endOffset);
        if (endLineNumber > 0) {
            type.setEndLine(endLineNumber);
        }

        // Extract content
        try {
            type.setContent(fullSource.substring(startOffset, startOffset + length));
        } catch (IndexOutOfBoundsException e) {
            type.setContent("");
        }

        // Determine type kind
        TypeKind kind;
        boolean isInterface = false;

        if (typeDecl instanceof TypeDeclaration) {
            TypeDeclaration classDecl = (TypeDeclaration) typeDecl;
            isInterface = classDecl.isInterface();
            kind = isInterface ? TypeKind.INTERFACE : TypeKind.STRUCT;
        } else if (typeDecl instanceof EnumDeclaration) {
            kind = TypeKind.ENUM;
        } else if (typeDecl instanceof AnnotationTypeDeclaration) {
            kind = TypeKind.TYPEDEF;
        } else {
            kind = TypeKind.STRUCT;
        }

        type.setTypeKind(kind);
        type.setExported(Modifier.isPublic(typeDecl.getModifiers()));

        // Resolve bindings for complete type information
        if (this.resolveBindings) {
            ITypeBinding binding = typeDecl.resolveBinding();
            if (binding != null) {
                // Register type binding in JavaModelHelper for later hierarchy queries
                String qualifiedName = binding.getQualifiedName();
                modelHelper.registerTypeBinding(qualifiedName, binding);
                modelHelper.registerTypeBinding(type.getName(), binding);

                if (verbose) {
                    System.err.println("✅ [TypeBinding] " + type.getName());
                    System.err.println("   Qualified: " + qualifiedName);
                    System.err.println("   Module: " + binding.getModule());
                    System.err.println("   Package: " + binding.getPackage().getName());
                }

                // Extract superclass (extends)
                if (binding.getSuperclass() != null &&
                    !binding.getSuperclass().getQualifiedName().equals("java.lang.Object")) {
                    ITypeBinding superClass = binding.getSuperclass();
                    Dependency dep = createDependency(superClass);
                    type.addInlineStruct(dep);
                    if (verbose) {
                        System.err.println("   Superclass: " + superClass.getQualifiedName());
                    }
                }

                // Extract interfaces (implements)
                ITypeBinding[] interfaces = binding.getInterfaces();
                for (ITypeBinding iface : interfaces) {
                    Dependency dep = createDependency(iface);
                    type.addImplement(dep);
                    if (verbose) {
                        System.err.println("   Interface: " + iface.getQualifiedName());
                    }
                }
            } else {
                if (verbose) {
                    System.err.println("⚠️  [TypeBinding] Failed to resolve binding for: " + type.getName());
                }
            }
        }

        // Extract methods
        if (typeDecl instanceof TypeDeclaration) {
            TypeDeclaration classDecl = (TypeDeclaration) typeDecl;
            MethodDeclaration[] methods = classDecl.getMethods();

            for (MethodDeclaration method : methods) {
                String methodName = method.getName().getIdentifier();
                String simpleKey = type.getName() + "." + methodName;

                // Handle method overloading
                String finalKey = simpleKey;
                if (pkg.getFunctions() != null && pkg.getFunctions().containsKey(simpleKey)) {
                    int count = 1;
                    while (pkg.getFunctions().containsKey(simpleKey + "#" + count)) {
                        count++;
                    }
                    finalKey = simpleKey + "#" + count;
                }

                Identity methodId = new Identity();
                methodId.setModPath(modPath);
                methodId.setPkgPath(pkgPath);
                methodId.setName(finalKey);

                type.addMethod(finalKey, methodId);

                Function function = convertMethodDeclaration(method, pkgPath, modPath,
                    filePath, fullSource, cu, type.getName(), finalKey);
                if (function != null) {
                    pkg.addFunction(finalKey, function);
                }
            }

            // Extract fields
            FieldDeclaration[] fields = classDecl.getFields();
            System.out.println(">>> DEBUG: Found " + fields.length + " fields in " + type.getName());
            for (FieldDeclaration field : fields) {
                for (Object frag : field.fragments()) {
                    if (frag instanceof VariableDeclarationFragment) {
                        VariableDeclarationFragment fragment = (VariableDeclarationFragment) frag;
                        Dependency fieldDep = createFieldDependency(
                            field, fragment, filePath, fullSource, cu
                        );
                        type.addSubStruct(fieldDep);

                        // Also add to top-level Functions (for ABCoder indexing)
                        // ABCoder reads from Functions, not Vars!
                        Var var = createVarFromField(field, fragment, filePath, fullSource, cu, modPath, pkgPath);
                        String varName = type.getName() + "." + fragment.getName().getIdentifier();
                        Function varAsFunction = varToFunction(var, varName, filePath, modPath, pkgPath);
                        pkg.addFunction(varName, varAsFunction);
                        System.err.println("DEBUG: Added field as function " + varName + " to package " + pkgPath);
                    }
                }
            }
        }

        return type;
    }

    /**
     * Create Var from FieldDeclaration (for top-level Vars indexing)
     */
    private Var createVarFromField(FieldDeclaration fieldDecl, VariableDeclarationFragment fragment,
                                   String filePath, String fullSource, CompilationUnit cu, String modPath, String pkgPath) {
        System.out.println(">>> createVarFromField called for: " + fragment.getName().getIdentifier() + " in " + pkgPath);
        Var var = new Var();

        int modifiers = fieldDecl.getModifiers();
        var.setExported(Modifier.isPublic(modifiers));
        var.setConst(false);
        var.setPointer(false);

        var.setModPath(modPath);
        var.setPkgPath(pkgPath);
        var.setName(fragment.getName().getIdentifier());
        var.setFile(filePath);

        int startOffset = fragment.getStartPosition();
        var.setStartOffset(startOffset);
        var.setEndOffset(startOffset + fragment.getLength());

        int lineNumber = cu.getLineNumber(startOffset);
        if (lineNumber > 0) {
            var.setLine(lineNumber);
        }

        // Set field type
        ITypeBinding typeBinding = fieldDecl.getType().resolveBinding();
        if (typeBinding != null) {
            Identity typeIdentity = createIdentity(typeBinding, modPath);
            var.setType(typeIdentity);
        }

        // Set content (field declaration code)
        try {
            var.setContent(fullSource.substring(startOffset, startOffset + fragment.getLength()));
        } catch (Exception e) {
            var.setContent("");
        }

        return var;
    }

    /**
     * Convert Var to Function (for ABCoder indexing)
     * ABCoder reads from Functions, so we need to convert fields to Functions
     */
    private Function varToFunction(Var var, String name, String filePath, String modPath, String pkgPath) {
        Function func = new Function();

        func.setExported(var.isExported());
        func.setMethod(false); // It's a field, not a method
        func.setModPath(modPath);
        func.setPkgPath(pkgPath);
        func.setName(name);
        func.setFile(filePath);
        func.setLine(var.getLine());
        func.setStartOffset(var.getStartOffset());
        func.setEndOffset(var.getEndOffset());
        func.setContent(var.getContent());

        // Build signature like a getter-less field
        StringBuilder sig = new StringBuilder();
        if (var.getType() != null) {
            sig.append(var.getType().getName());
        } else {
            sig.append("unknown");
        }
        sig.append(" ").append(var.getName());
        func.setSignature(sig.toString());

        return func;
    }

    /**
     * Convert MethodDeclaration to Function
     */
    public Function convertMethodDeclaration(MethodDeclaration method, String pkgPath,
                                             String modPath, String filePath, String fullSource,
                                             CompilationUnit cu, String typeName, String fullMethodName) {
        Function function = new Function();

        function.setModPath(modPath);
        function.setPkgPath(pkgPath);
        function.setName(fullMethodName);

        int startOffset = method.getStartPosition();
        int length = method.getLength();
        function.setFile(filePath);
        function.setStartOffset(startOffset);
        function.setEndOffset(startOffset + length);

        int lineNumber = cu.getLineNumber(startOffset);
        if (lineNumber > 0) {
            function.setLine(lineNumber);
        }

        int endOffset = startOffset + length;
        int endLineNumber = cu.getLineNumber(endOffset);
        if (endLineNumber > 0) {
            function.setEndLine(endLineNumber);
        }

        try {
            function.setContent(fullSource.substring(startOffset, startOffset + length));
        } catch (IndexOutOfBoundsException e) {
            function.setContent("");
        }

        function.setExported(Modifier.isPublic(method.getModifiers()));
        function.setMethod(true);

        // 处理方法上的注解
        processMethodAnnotations(method, function, cu);

        // Build signature
        StringBuilder sig = new StringBuilder();
        if (method.getReturnType2() != null) {
            sig.append(method.getReturnType2().toString()).append(" ");
        }
        sig.append(method.getName().getIdentifier());
        sig.append("(");
        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> params = method.parameters();
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sig.append(", ");
            SingleVariableDeclaration param = params.get(i);
            sig.append(param.getType().toString()).append(" ").append(param.getName().getIdentifier());
        }
        sig.append(")");
        function.setSignature(sig.toString());

        // Add receiver for instance methods
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        if (!isStatic) {
            Receiver receiver = new Receiver();
            receiver.setPointer(false);

            Identity typeIdentity = new Identity();
            typeIdentity.setModPath(modPath);
            typeIdentity.setPkgPath(pkgPath);
            typeIdentity.setName(typeName);

            receiver.setType(typeIdentity);
            function.setReceiver(receiver);
        }

        // Resolve method binding
        if (this.resolveBindings) {
            IMethodBinding binding = method.resolveBinding();
            if (binding != null) {
                ITypeBinding returnType = binding.getReturnType();
                if (returnType != null) {
                    Dependency resultDep = createDependency(returnType);
                    function.addResult(resultDep);
                }

                ITypeBinding declaringClass = binding.getDeclaringClass();
                if (declaringClass != null && declaringClass.isInterface()) {
                    function.setInterfaceMethod(true);
                }
            }
        }

        // Parameters
        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> paramDecls = method.parameters();
        for (SingleVariableDeclaration param : paramDecls) {
            Dependency paramDep = new Dependency();

            ITypeBinding paramType = param.getType().resolveBinding();
            if (paramType != null) {
                paramDep.setModPath(createIdentity(paramType).getModPath());
                paramDep.setPkgPath(createIdentity(paramType).getPkgPath());
                paramDep.setName(paramType.getName());
            } else {
                String paramTypeName = param.getType().toString();
                if (paramTypeName.contains(".")) {
                    paramTypeName = paramTypeName.substring(paramTypeName.lastIndexOf('.') + 1);
                }
                paramDep.setName(paramTypeName);
                paramDep.setPkgPath(pkgPath);
                paramDep.setModPath(modPath);
            }

            int paramStart = param.getStartPosition();
            int paramLine = cu.getLineNumber(paramStart);
            if (paramLine > 0) {
                paramDep.setLine(paramLine);
            }

            function.addParam(paramDep);
        }

        // Process thrown exception types (throws clause)
        @SuppressWarnings("unchecked")
        List<Type> thrownExceptions = method.thrownExceptionTypes();
        for (Type exceptionType : thrownExceptions) {
            ITypeBinding exceptionBinding = exceptionType.resolveBinding();
            if (exceptionBinding != null) {
                Dependency exceptionDep = createDependency(exceptionBinding);
                function.addType(exceptionDep);
            }
        }

        // Analyze method body dependencies (if MethodBodyAnalyzer is provided)
        if (methodBodyAnalyzer != null && method.getBody() != null) {
            methodBodyAnalyzer.collectMethodBodyDependencies(
                method.getBody(), function, pkgPath, modPath, filePath, cu
            );
        }

        return function;
    }

    /**
     * Create Identity from ITypeBinding
     */
    public Identity createIdentity(ITypeBinding typeBinding) {
        return createIdentity(typeBinding, this.moduleName);
    }

    /**
     * Create Identity from ITypeBinding with explicit project name
     */
    public Identity createIdentity(ITypeBinding typeBinding, String projectName) {
        Identity id = new Identity();

        IModuleBinding module = typeBinding.getModule();
        if (module != null && module.getName() != null && !module.getName().isEmpty()) {
            id.setModPath(module.getName());
        } else if (typeBinding.isFromSource()) {
            id.setModPath(projectName);
        } else {
            id.setModPath(inferLibraryModPath(typeBinding));
        }

        IPackageBinding pkg = typeBinding.getPackage();
        if (pkg != null && pkg.getName() != null && !pkg.getName().isEmpty()) {
            id.setPkgPath(pkg.getName());
        } else {
            id.setPkgPath("");
        }

        id.setName(typeBinding.getName());

        return id;
    }

    /**
     * Create Dependency from ITypeBinding
     */
    public Dependency createDependency(ITypeBinding typeBinding) {
        Dependency dep = new Dependency();

        Identity id = createIdentity(typeBinding);
        dep.setModPath(id.getModPath());
        dep.setPkgPath(id.getPkgPath());
        dep.setName(id.getName());

        return dep;
    }

    /**
     * Create Dependency for field
     */
    public Dependency createFieldDependency(FieldDeclaration fieldDecl,
                                            VariableDeclarationFragment fragment,
                                            String filePath,
                                            String fullSource,
                                            CompilationUnit cu) {
        Dependency dep = new Dependency();

        dep.setKind("Field");

        IVariableBinding varBinding = fragment.resolveBinding();
        if (varBinding != null && varBinding.getType() != null) {
            ITypeBinding typeBinding = varBinding.getType();
            Identity typeId = createIdentity(typeBinding);

            dep.setName(fragment.getName().getIdentifier());
            dep.setModPath(typeId.getModPath());
            dep.setPkgPath(typeId.getPkgPath());
        } else {
            dep.setName(fragment.getName().getIdentifier());
            dep.setModPath("");
            dep.setPkgPath("");
        }

        int fieldStart = fragment.getStartPosition();
        int lineNumber = cu.getLineNumber(fieldStart);

        if (lineNumber > 0) {
            dep.setLine(lineNumber);
        }

        String fullSignature = extractFieldSignature(fieldDecl, fragment);
        dep.setDesc(fullSignature);

        try {
            String fieldContent = fullSource.substring(fieldStart, fieldStart + fragment.getLength());
            dep.setCodes(fieldContent);
        } catch (IndexOutOfBoundsException e) {
            dep.setCodes("");
        }

        return dep;
    }

    /**
     * Extract full field signature
     */
    private String extractFieldSignature(FieldDeclaration fieldDecl,
                                         VariableDeclarationFragment fragment) {
        StringBuilder signature = new StringBuilder();

        int modifiers = fieldDecl.getModifiers();

        if (Modifier.isPrivate(modifiers)) {
            signature.append("private ");
        } else if (Modifier.isPublic(modifiers)) {
            signature.append("public ");
        } else if (Modifier.isProtected(modifiers)) {
            signature.append("protected ");
        }

        if (Modifier.isStatic(modifiers)) signature.append("static ");
        if (Modifier.isFinal(modifiers)) signature.append("final ");
        if (Modifier.isTransient(modifiers)) signature.append("transient ");
        if (Modifier.isVolatile(modifiers)) signature.append("volatile ");

        signature.append(fieldDecl.getType().toString())
                 .append(" ")
                 .append(fragment.getName().getIdentifier());

        return signature.toString();
    }

    /**
     * Infer library module path from type binding
     */
    private String inferLibraryModPath(ITypeBinding typeBinding) {
        IPackageBinding pkg = typeBinding.getPackage();
        if (pkg == null) {
            return "java.base";
        }

        String pkgName = pkg.getName();

        if (pkgName.startsWith("java.")) {
            return "java.base";
        } else if (pkgName.startsWith("javax.")) {
            return "java.se";
        } else if (pkgName.startsWith("jdk.")) {
            return "jdk.internal";
        }

        // Common third-party libraries
        if (pkgName.startsWith("org.springframework.")) {
            return "spring-framework";
        } else if (pkgName.startsWith("com.google.common.")) {
            return "guava";
        } else if (pkgName.startsWith("org.apache.commons.")) {
            return "apache-commons";
        } else if (pkgName.startsWith("com.fasterxml.jackson.")) {
            return "jackson";
        } else if (pkgName.startsWith("org.hibernate.")) {
            return "hibernate";
        } else if (pkgName.startsWith("org.junit.")) {
            return "junit";
        } else if (pkgName.startsWith("lombok.")) {
            return "lombok";
        }

        // Fallback
        String[] parts = pkgName.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        } else if (parts.length == 1) {
            return parts[0];
        }

        return "java.base";
    }

    /**
     * 处理方法上的注解 (Modifier中的注解)
     */
    @SuppressWarnings("unchecked")
    private void processMethodAnnotations(MethodDeclaration method, Function function, CompilationUnit cu) {
        // 获取方法上的所有注解 (modifiers)
        for (Object modifierObj : method.modifiers()) {
            if (modifierObj instanceof MarkerAnnotation) {
                MarkerAnnotation ann = (MarkerAnnotation) modifierObj;
                ITypeBinding binding = ann.resolveTypeBinding();
                if (binding != null) {
                    if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                        if (verbose) {
                            System.err.println("   ⏭️  [MethodAnnotation] Skipping standard library: " + binding.getQualifiedName());
                        }
                        continue;
                    }

                    Dependency dep = createDependency(binding);
                    function.addType(dep);

                    if (verbose) {
                        System.err.println("   🏷️  [MethodAnnotation] " + binding.getQualifiedName());
                    }
                }
            } else if (modifierObj instanceof NormalAnnotation) {
                NormalAnnotation ann = (NormalAnnotation) modifierObj;
                ITypeBinding binding = ann.resolveTypeBinding();
                if (binding != null) {
                    if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                        continue;
                    }

                    Dependency dep = createDependency(binding);
                    function.addType(dep);

                    if (verbose) {
                        System.err.println("   🏷️  [MethodAnnotation] " + binding.getQualifiedName());
                    }
                }
            } else if (modifierObj instanceof SingleMemberAnnotation) {
                SingleMemberAnnotation ann = (SingleMemberAnnotation) modifierObj;
                ITypeBinding binding = ann.resolveTypeBinding();
                if (binding != null) {
                    if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                        continue;
                    }

                    Dependency dep = createDependency(binding);
                    function.addType(dep);

                    if (verbose) {
                        System.err.println("   🏷️  [MethodAnnotation] " + binding.getQualifiedName());
                    }
                }
            }
        }
    }
}
