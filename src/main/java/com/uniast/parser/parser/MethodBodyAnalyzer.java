package com.uniast.parser.parser;

import com.uniast.parser.model.*;
import com.uniast.parser.helper.JavaModelHelper;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;

/**
 * 方法体依赖分析器
 * 负责提取方法内部的依赖关系（functionCalls, methodCalls, types, globalVars）
 */
public class MethodBodyAnalyzer {

    private final JdtAstConverter converter;
    private final JavaModelHelper modelHelper;
    private final boolean verbose;
    private final boolean excludeStandardLibraries;
    private final PrintWriter logWriter;

    public MethodBodyAnalyzer(JdtAstConverter converter, JavaModelHelper modelHelper,
                             boolean verbose, boolean excludeStandardLibraries) {
        this.converter = converter;
        this.modelHelper = modelHelper;
        this.verbose = verbose;
        this.excludeStandardLibraries = excludeStandardLibraries;

        // 初始化日志文件
        if (verbose) {
            try {
                File logDir = new File("/tmp/java-parser");
                if (!logDir.exists()) {
                    logDir.mkdirs();
                }
                File logFile = new File(logDir, "method-body-analyzer.log");
                this.logWriter = new PrintWriter(new FileWriter(logFile, false));
                logWriter.println("=== MethodBodyAnalyzer Log Started at " + java.time.LocalDateTime.now() + " ===");
                logWriter.flush();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create log file", e);
            }
        } else {
            this.logWriter = null;
        }
    }

    /**
     * 输出日志到文件 (verbose 模式下)
     */
    private void log(String message) {
        if (verbose && logWriter != null) {
            logWriter.println(message);
            logWriter.flush();
            // 同时输出到 stderr
            System.err.println(message);
        }
    }

    /**
     * 收集方法体内的依赖
     */
    public void collectMethodBodyDependencies(Block methodBody, Function function,
                                              String pkgPath, String modPath,
                                              String filePath, CompilationUnit cu) {
        final String functionName = function.getName();

        if (verbose) {
            System.err.println("🔍 [Verbose] Collecting dependencies for function: " + functionName);
            System.err.println("   Package: " + pkgPath + ", Module: " + modPath);
        }

        methodBody.accept(new ASTVisitor(true) {
            @Override
            public boolean visit(MethodInvocation node) {
                IMethodBinding binding = node.resolveMethodBinding();
                if (binding != null) {
                    ITypeBinding declaringClass = binding.getDeclaringClass();
                    if (declaringClass != null) {
                        if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(declaringClass)) {
                            if (verbose) {
                                System.err.println("   ⏭️  [MethodInvocation] Skipping standard library: " +
                                    declaringClass.getQualifiedName() + "." + binding.getName());
                            }
                            return true;
                        }

                        Identity typeId = converter.createIdentity(declaringClass);
                        String callName = typeId.getName() + "." + binding.getName();

                        Dependency dep = new Dependency();
                        dep.setModPath(typeId.getModPath());
                        dep.setPkgPath(typeId.getPkgPath());
                        dep.setName(callName);
                        dep.setLine(cu.getLineNumber(node.getStartPosition()));

                        boolean isStatic = Modifier.isStatic(binding.getModifiers());

                        if (verbose) {
                            System.err.println("   📞 [MethodInvocation] " + callName);
                            System.err.println("      Declaring class: " + declaringClass.getQualifiedName());
                            System.err.println("      Module: " + typeId.getModPath());
                            System.err.println("      Package: " + typeId.getPkgPath());
                            System.err.println("      Is static: " + isStatic);
                        }

                        if (isStatic || node.getExpression() == null) {
                            function.addFunctionCall(dep);
                            if (verbose) {
                                System.err.println("      → Added to functionCalls");
                            }
                        } else {
                            function.addMethodCall(dep);
                            if (verbose) {
                                System.err.println("      → Added to methodCalls");
                            }
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean visit(SimpleType node) {
                ITypeBinding binding = node.resolveBinding();
                if (binding != null) {
                    if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                        if (verbose) {
                            System.err.println("   ⏭️  [SimpleType] Skipping standard library: " + binding.getQualifiedName());
                        }
                        return true;
                    }

                    Identity typeId = converter.createIdentity(binding);
                    Dependency dep = new Dependency();
                    dep.setModPath(typeId.getModPath());
                    dep.setPkgPath(typeId.getPkgPath());
                    dep.setName(typeId.getName());
                    dep.setLine(cu.getLineNumber(node.getStartPosition()));
                    function.addType(dep);

                    if (verbose) {
                        System.err.println("   📦 [SimpleType] " + binding.getQualifiedName());
                        System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                    }
                }
                return true;
            }

            @Override
            public boolean visit(FieldAccess node) {
                IVariableBinding binding = node.resolveFieldBinding();
                if (binding != null && binding.isField()) {
                    ITypeBinding declaringClass = binding.getDeclaringClass();
                    if (declaringClass != null) {
                        Identity typeId = converter.createIdentity(declaringClass);
                        String fieldName = typeId.getName() + "." + binding.getName();

                        Dependency dep = new Dependency();
                        dep.setModPath(typeId.getModPath());
                        dep.setPkgPath(typeId.getPkgPath());
                        dep.setName(fieldName);
                        dep.setLine(cu.getLineNumber(node.getStartPosition()));

                        function.addGlobalVar(dep);

                        if (verbose) {
                            System.err.println("   🏷️  [FieldAccess] " + fieldName);
                            System.err.println("      Declaring class: " + declaringClass.getQualifiedName());
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean visit(FieldDeclaration node) {
                org.eclipse.jdt.core.dom.Type fieldType = node.getType();
                if (fieldType instanceof SimpleType) {
                    ITypeBinding binding = ((SimpleType) fieldType).resolveBinding();
                    if (binding != null) {
                        if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                            if (verbose) {
                                System.err.println("   ⏭️  [FieldDeclaration] Skipping standard library: " +
                                    binding.getQualifiedName());
                            }
                            return true;
                        }

                        Identity typeId = converter.createIdentity(binding);
                        Dependency dep = new Dependency();
                        dep.setModPath(typeId.getModPath());
                        dep.setPkgPath(typeId.getPkgPath());
                        dep.setName(typeId.getName());
                        dep.setLine(cu.getLineNumber(node.getStartPosition()));

                        function.addType(dep);

                        if (verbose) {
                            String fieldName = "";
                            if (node.fragments().size() > 0) {
                                fieldName = ((VariableDeclarationFragment)node.fragments().get(0)).getName().getIdentifier();
                            }
                            System.err.println("   📦 [FieldDeclaration] " + binding.getQualifiedName());
                            System.err.println("      Field name: " + fieldName);
                            System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean visit(SingleVariableDeclaration node) {
                org.eclipse.jdt.core.dom.Type paramType = node.getType();
                if (paramType instanceof SimpleType) {
                    ITypeBinding binding = ((SimpleType) paramType).resolveBinding();
                    if (binding != null) {
                        if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                            if (verbose) {
                                System.err.println("   ⏭️  [SingleVariableDeclaration] Skipping standard library: " +
                                    binding.getQualifiedName());
                            }
                            return true;
                        }

                        Identity typeId = converter.createIdentity(binding);
                        Dependency dep = new Dependency();
                        dep.setModPath(typeId.getModPath());
                        dep.setPkgPath(typeId.getPkgPath());
                        dep.setName(typeId.getName());
                        dep.setLine(cu.getLineNumber(node.getStartPosition()));

                        function.addType(dep);

                        if (verbose) {
                            System.err.println("   📦 [SingleVariableDeclaration] " + binding.getQualifiedName());
                            System.err.println("      Parameter name: " + node.getName().getIdentifier());
                            System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                IMethodBinding methodBinding = node.resolveBinding();
                if (methodBinding != null) {
                    ITypeBinding returnTypeBinding = methodBinding.getReturnType();
                    if (returnTypeBinding != null) {
                        if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(returnTypeBinding)) {
                            if (verbose) {
                                System.err.println("   ⏭️  [MethodDeclaration.ReturnType] Skipping standard library: " +
                                    returnTypeBinding.getQualifiedName());
                            }
                            return true;
                        }

                        Identity typeId = converter.createIdentity(returnTypeBinding);
                        Dependency dep = new Dependency();
                        dep.setModPath(typeId.getModPath());
                        dep.setPkgPath(typeId.getPkgPath());
                        dep.setName(typeId.getName());
                        dep.setLine(cu.getLineNumber(node.getStartPosition()));

                        function.addType(dep);

                        if (verbose) {
                            System.err.println("   📦 [MethodDeclaration.ReturnType] " + returnTypeBinding.getQualifiedName());
                            System.err.println("      Method name: " + node.getName().getIdentifier());
                            System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean visit(CastExpression node) {
                org.eclipse.jdt.core.dom.Type castType = node.getType();
                ITypeBinding binding = null;

                // 扩展处理所有类型: SimpleType, ParameterizedType, ArrayType
                if (castType instanceof SimpleType) {
                    binding = ((SimpleType) castType).resolveBinding();
                } else if (castType instanceof org.eclipse.jdt.core.dom.ParameterizedType) {
                    // 处理: (List<String>) obj
                    binding = ((org.eclipse.jdt.core.dom.ParameterizedType) castType).resolveBinding();
                } else if (castType instanceof org.eclipse.jdt.core.dom.ArrayType) {
                    // 处理: (String[]) obj
                    binding = ((org.eclipse.jdt.core.dom.ArrayType) castType).resolveBinding();
                }

                if (binding != null) {
                    if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                        if (verbose) {
                            System.err.println("   ⏭️  [CastExpression] Skipping standard library: " +
                                binding.getQualifiedName());
                        }
                        return true;
                    }

                    Identity typeId = converter.createIdentity(binding);
                    Dependency dep = new Dependency();
                    dep.setModPath(typeId.getModPath());
                    dep.setPkgPath(typeId.getPkgPath());
                    dep.setName(typeId.getName());
                    dep.setLine(cu.getLineNumber(node.getStartPosition()));

                    function.addType(dep);

                    if (verbose) {
                        System.err.println("   🔄 [CastExpression] " + binding.getQualifiedName());
                        System.err.println("      Cast to: " + binding.getName());
                        System.err.println("      Type class: " + castType.getClass().getSimpleName());
                        System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                    }
                }
                return true;
            }

            @Override
            public boolean visit(QualifiedName node) {
                IBinding binding = node.resolveBinding();
                if (binding instanceof IVariableBinding) {
                    IVariableBinding varBinding = (IVariableBinding) binding;
                    // 处理枚举常量访问 (例如: Language.JAVA)
                    if (varBinding.isEnumConstant()) {
                        ITypeBinding enumType = varBinding.getDeclaringClass();
                        if (enumType != null) {
                            if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(enumType)) {
                                if (verbose) {
                                    System.err.println("   ⏭️  [QualifiedName] Skipping standard library enum: " +
                                        enumType.getQualifiedName() + "." + varBinding.getName());
                                }
                                return true;
                            }

                            Identity typeId = converter.createIdentity(enumType);
                            Dependency dep = new Dependency();
                            dep.setModPath(typeId.getModPath());
                            dep.setPkgPath(typeId.getPkgPath());
                            dep.setName(typeId.getName());
                            dep.setLine(cu.getLineNumber(node.getStartPosition()));

                            function.addType(dep);

                            if (verbose) {
                                System.err.println("   📦 [QualifiedName] Enum: " + enumType.getQualifiedName());
                                System.err.println("      Enum constant: " + varBinding.getName());
                                System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                            }
                        }
                    }
                    // 处理静态常量访问 (例如: SomeClass.CONSTANT)
                    else if (varBinding.isField() && Modifier.isStatic(varBinding.getModifiers())) {
                        ITypeBinding declaringClass = varBinding.getDeclaringClass();
                        if (declaringClass != null) {
                            if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(declaringClass)) {
                                if (verbose) {
                                    System.err.println("   ⏭️  [QualifiedName] Skipping standard library static field: " +
                                        declaringClass.getQualifiedName() + "." + varBinding.getName());
                                }
                                return true;
                            }

                            Identity typeId = converter.createIdentity(declaringClass);
                            Dependency dep = new Dependency();
                            dep.setModPath(typeId.getModPath());
                            dep.setPkgPath(typeId.getPkgPath());
                            dep.setName(typeId.getName());
                            dep.setLine(cu.getLineNumber(node.getStartPosition()));

                            function.addType(dep);

                            if (verbose) {
                                System.err.println("   📦 [QualifiedName] Static field type: " + declaringClass.getQualifiedName());
                                System.err.println("      Field name: " + varBinding.getName());
                                System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                            }
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean visit(ClassInstanceCreation node) {
                // 处理泛型实例化: new ArrayList<String>(), new User()
                ITypeBinding binding = node.getType().resolveBinding();
                if (binding != null) {
                    if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                        if (verbose) {
                            System.err.println("   ⏭️  [ClassInstanceCreation] Skipping standard library: " +
                                binding.getQualifiedName());
                        }
                        return true;
                    }

                    Identity typeId = converter.createIdentity(binding);
                    Dependency dep = new Dependency();
                    dep.setModPath(typeId.getModPath());
                    dep.setPkgPath(typeId.getPkgPath());
                    dep.setName(typeId.getName());
                    dep.setLine(cu.getLineNumber(node.getStartPosition()));

                    function.addType(dep);

                    if (verbose) {
                        System.err.println("   🆕 [ClassInstanceCreation] " + binding.getQualifiedName());
                        System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                    }
                }

                // 递归遍历 expression (处理链式: new User().setName())
                if (node.getExpression() != null) {
                    node.getExpression().accept(this);
                }

                return true;
            }

            @Override
            public boolean visit(MarkerAnnotation node) {
                // 处理注解: @Deprecated, @Override
                ITypeBinding binding = node.resolveTypeBinding();
                if (binding != null) {
                    if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                        if (verbose) {
                            System.err.println("   ⏭️  [MarkerAnnotation] Skipping standard library: " +
                                binding.getQualifiedName());
                        }
                        return true;
                    }

                    Identity typeId = converter.createIdentity(binding);
                    Dependency dep = new Dependency();
                    dep.setModPath(typeId.getModPath());
                    dep.setPkgPath(typeId.getPkgPath());
                    dep.setName(typeId.getName());
                    dep.setLine(cu.getLineNumber(node.getStartPosition()));

                    function.addType(dep);

                    if (verbose) {
                        System.err.println("   🏷️  [MarkerAnnotation] " + binding.getQualifiedName());
                        System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                    }
                }
                return true;
            }

            @Override
            public boolean visit(NormalAnnotation node) {
                // 处理带属性的注解: @MyAnnotation(value = "x")
                ITypeBinding binding = node.resolveTypeBinding();
                if (binding != null) {
                    if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                        if (verbose) {
                            System.err.println("   ⏭️  [NormalAnnotation] Skipping standard library: " +
                                binding.getQualifiedName());
                        }
                        return true;
                    }

                    Identity typeId = converter.createIdentity(binding);
                    Dependency dep = new Dependency();
                    dep.setModPath(typeId.getModPath());
                    dep.setPkgPath(typeId.getPkgPath());
                    dep.setName(typeId.getName());
                    dep.setLine(cu.getLineNumber(node.getStartPosition()));

                    function.addType(dep);

                    if (verbose) {
                        System.err.println("   🏷️  [NormalAnnotation] " + binding.getQualifiedName());
                        System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                    }
                }
                return true;
            }

            @Override
            public boolean visit(SingleMemberAnnotation node) {
                // 处理单成员注解: @Deprecated("msg")
                ITypeBinding binding = node.resolveTypeBinding();
                if (binding != null) {
                    if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(binding)) {
                        if (verbose) {
                            System.err.println("   ⏭️  [SingleMemberAnnotation] Skipping standard library: " +
                                binding.getQualifiedName());
                        }
                        return true;
                    }

                    Identity typeId = converter.createIdentity(binding);
                    Dependency dep = new Dependency();
                    dep.setModPath(typeId.getModPath());
                    dep.setPkgPath(typeId.getPkgPath());
                    dep.setName(typeId.getName());
                    dep.setLine(cu.getLineNumber(node.getStartPosition()));

                    function.addType(dep);

                    if (verbose) {
                        System.err.println("   🏷️  [SingleMemberAnnotation] " + binding.getQualifiedName());
                        System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                    }
                }
                return true;
            }

            @Override
            public boolean visit(ThrowStatement node) {
                Expression expression = node.getExpression();
                if (expression != null) {
                    ITypeBinding exceptionBinding = expression.resolveTypeBinding();
                    if (exceptionBinding != null) {
                        if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(exceptionBinding)) {
                            if (verbose) {
                                System.err.println("   ⏭️  [ThrowStatement] Skipping standard library exception: " +
                                    exceptionBinding.getQualifiedName());
                            }
                            return true;
                        }

                        Identity typeId = converter.createIdentity(exceptionBinding);
                        Dependency dep = new Dependency();
                        dep.setModPath(typeId.getModPath());
                        dep.setPkgPath(typeId.getPkgPath());
                        dep.setName(typeId.getName());
                        dep.setLine(cu.getLineNumber(node.getStartPosition()));

                        function.addType(dep);

                        if (verbose) {
                            System.err.println("   💥 [ThrowStatement] " + exceptionBinding.getQualifiedName());
                            System.err.println("      Exception type: " + exceptionBinding.getName());
                            System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean visit(TryStatement node) {
                // Process try-with-resources
                @SuppressWarnings("unchecked")
                List<VariableDeclarationExpression> resources = node.resources();
                for (VariableDeclarationExpression resource : resources) {
                    ITypeBinding resourceBinding = resource.resolveTypeBinding();
                    if (resourceBinding != null) {
                        if (excludeStandardLibraries && modelHelper.isJavaStandardLibrary(resourceBinding)) {
                            if (verbose) {
                                System.err.println("   ⏭️  [TryStatement.Resource] Skipping standard library: " +
                                    resourceBinding.getQualifiedName());
                            }
                            continue;
                        }

                        Identity typeId = converter.createIdentity(resourceBinding);
                        Dependency dep = new Dependency();
                        dep.setModPath(typeId.getModPath());
                        dep.setPkgPath(typeId.getPkgPath());
                        dep.setName(typeId.getName());
                        dep.setLine(cu.getLineNumber(node.getStartPosition()));

                        function.addType(dep);

                        if (verbose) {
                            System.err.println("   🔧 [TryStatement.Resource] " + resourceBinding.getQualifiedName());
                            System.err.println("      Resource type: " + resourceBinding.getName());
                            System.err.println("      Module: " + typeId.getModPath() + ", Package: " + typeId.getPkgPath());
                        }
                    }
                }
                return true;
            }
        });

        if (verbose) {
            System.err.println("   ✅ Completed dependency collection for: " + functionName);
            System.err.println("      Total functionCalls: " + function.getFunctionCalls().size());
            System.err.println("      Total methodCalls: " + function.getMethodCalls().size());
            System.err.println("      Total types: " + function.getTypes().size());
            System.err.println("      Total globalVars: " + function.getGlobalVars().size());
        }
    }
}
