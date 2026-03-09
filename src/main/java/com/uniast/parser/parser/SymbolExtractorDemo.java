package com.uniast.parser.parser;

import com.uniast.parser.model.*;
import org.eclipse.jdt.core.dom.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo: 正确提取顶层符号
 * - 提取 TYPE（类、接口、枚举）
 * - 提取 FUNC（方法）
 * - 提取 VAR（字段）
 * - 不提取局部变量
 */
public class SymbolExtractorDemo {

    public static class SymbolInfo {
        public String name;
        public String type; // TYPE, FUNC, VAR
        public int line;
        public String signature;

        public SymbolInfo(String name, String type, int line, String signature) {
            this.name = name;
            this.type = type;
            this.line = line;
            this.signature = signature;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s (line %d): %s",
                type, name, line, signature != null ? signature : "");
        }
    }

    /**
     * 提取文件的顶层符号
     */
    public static List<SymbolInfo> extractTopLevelSymbols(String filePath) throws IOException {
        String source = Files.readString(Paths.get(filePath));

        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setSource(source.toCharArray());
        parser.setResolveBindings(false); // 不需要绑定，只提取结构

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        List<SymbolInfo> symbols = new ArrayList<>();

        // 使用 Visitor 遍历 AST
        cu.accept(new ASTVisitor() {
            private String currentTypeName = null;

            @Override
            public boolean visit(TypeDeclaration node) {
                // TYPE: 类或接口
                String typeName = node.getName().getIdentifier();
                String kind = node.isInterface() ? "INTERFACE" : "CLASS";
                int line = cu.getLineNumber(node.getStartPosition());

                symbols.add(new SymbolInfo(
                    typeName,
                    "TYPE",
                    line,
                    node.isInterface() ? "interface " + typeName : "class " + typeName
                ));

                // 提取字段（VAR）
                FieldDeclaration[] fields = node.getFields();
                for (FieldDeclaration field : fields) {
                    for (Object frag : field.fragments()) {
                        if (frag instanceof VariableDeclarationFragment) {
                            VariableDeclarationFragment fragment = (VariableDeclarationFragment) frag;
                            String fieldName = fragment.getName().getIdentifier();
                            int fieldLine = cu.getLineNumber(field.getStartPosition());

                            // 构建字段签名
                            String signature = buildFieldSignature(field, fieldName);

                            symbols.add(new SymbolInfo(
                                typeName + "." + fieldName,
                                "VAR",
                                fieldLine,
                                signature
                            ));
                        }
                    }
                }

                // 记录当前类名，用于方法名前缀
                currentTypeName = typeName;

                return true;
            }

            @Override
            public void endVisit(TypeDeclaration node) {
                currentTypeName = null;
            }

            @Override
            public boolean visit(EnumDeclaration node) {
                // TYPE: 枚举
                String typeName = node.getName().getIdentifier();
                int line = cu.getLineNumber(node.getStartPosition());

                symbols.add(new SymbolInfo(
                    typeName,
                    "TYPE",
                    line,
                    "enum " + typeName
                ));

                currentTypeName = typeName;
                return true;
            }

            @Override
            public void endVisit(EnumDeclaration node) {
                currentTypeName = null;
            }

            @Override
            public boolean visit(AnnotationTypeDeclaration node) {
                // TYPE: 注解
                String typeName = node.getName().getIdentifier();
                int line = cu.getLineNumber(node.getStartPosition());

                symbols.add(new SymbolInfo(
                    typeName,
                    "TYPE",
                    line,
                    "@interface " + typeName
                ));

                currentTypeName = typeName;
                return true;
            }

            @Override
            public void endVisit(AnnotationTypeDeclaration node) {
                currentTypeName = null;
            }

            @Override
            public boolean visit(MethodDeclaration node) {
                // FUNC: 方法
                if (currentTypeName == null) {
                    // 跳过不在类中的方法（理论上不应该出现）
                    return false;
                }

                String methodName = node.getName().getIdentifier();
                int line = cu.getLineNumber(node.getStartPosition());

                // 构建方法签名
                String signature = buildMethodSignature(node);

                symbols.add(new SymbolInfo(
                    currentTypeName + "." + methodName,
                    "FUNC",
                    line,
                    signature
                ));

                // 关键：返回 false，不遍历方法体，避免提取局部变量
                return false;
            }

            // 不需要 visit(VariableDeclarationFragment)，因为已在字段遍历中处理

            private String buildFieldSignature(FieldDeclaration field, String fieldName) {
                StringBuilder sb = new StringBuilder();

                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers)) sb.append("public ");
                else if (Modifier.isPrivate(modifiers)) sb.append("private ");
                else if (Modifier.isProtected(modifiers)) sb.append("protected ");

                if (Modifier.isStatic(modifiers)) sb.append("static ");
                if (Modifier.isFinal(modifiers)) sb.append("final ");
                if (Modifier.isTransient(modifiers)) sb.append("transient ");
                if (Modifier.isVolatile(modifiers)) sb.append("volatile ");

                sb.append(field.getType().toString()).append(" ").append(fieldName);

                return sb.toString();
            }

            private String buildMethodSignature(MethodDeclaration method) {
                StringBuilder sb = new StringBuilder();

                int modifiers = method.getModifiers();
                if (Modifier.isPublic(modifiers)) sb.append("public ");
                else if (Modifier.isPrivate(modifiers)) sb.append("private ");
                else if (Modifier.isProtected(modifiers)) sb.append("protected ");

                if (Modifier.isStatic(modifiers)) sb.append("static ");
                if (Modifier.isFinal(modifiers)) sb.append("final ");
                if (Modifier.isSynchronized(modifiers)) sb.append("synchronized ");

                if (method.getReturnType2() != null) {
                    sb.append(method.getReturnType2().toString()).append(" ");
                } else if (method.isConstructor()) {
                    // 构造函数没有返回类型
                } else {
                    sb.append("void ");
                }

                sb.append(method.getName().getIdentifier()).append("(");

                @SuppressWarnings("unchecked")
                List<SingleVariableDeclaration> params = method.parameters();
                for (int i = 0; i < params.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(params.get(i).getType().toString());
                    sb.append(" ");
                    sb.append(params.get(i).getName().getIdentifier());
                }

                sb.append(")");

                return sb.toString();
            }
        });

        return symbols;
    }

    public static void main(String[] args) throws IOException {
        // 测试：解析 JdtAstConverter.java
        String testFile = "src/main/java/com/uniast/parser/parser/JdtAstConverter.java";

        System.out.println("=== 测试文件: " + testFile + " ===\n");

        List<SymbolInfo> symbols = extractTopLevelSymbols(testFile);

        System.out.println("【TYPE 类/接口/枚举】");
        symbols.stream()
            .filter(s -> "TYPE".equals(s.type))
            .forEach(System.out::println);

        System.out.println("\n【VAR 字段】");
        symbols.stream()
            .filter(s -> "VAR".equals(s.type))
            .forEach(System.out::println);

        System.out.println("\n【FUNC 方法】");
        symbols.stream()
            .filter(s -> "FUNC".equals(s.type))
            .forEach(System.out::println);

        System.out.println("\n=== 统计 ===");
        System.out.println("TYPE: " + symbols.stream().filter(s -> "TYPE".equals(s.type)).count());
        System.out.println("VAR: " + symbols.stream().filter(s -> "VAR".equals(s.type)).count());
        System.out.println("FUNC: " + symbols.stream().filter(s -> "FUNC".equals(s.type)).count());
        System.out.println("Total: " + symbols.size());

        // 验证：检查是否有局部变量混入
        System.out.println("\n=== 验证：检查局部变量 ===");
        String[] localVars = {"om", "repoPathAbs", "javaFiles", "conv", "analyzer", "sb", "binding"};
        boolean hasLocal = false;
        for (String var : localVars) {
            if (symbols.stream().anyMatch(s -> s.name.endsWith("." + var))) {
                System.out.println("❌ 发现局部变量: " + var);
                hasLocal = true;
            }
        }
        if (!hasLocal) {
            System.out.println("✅ 无局部变量混入");
        }
    }
}
