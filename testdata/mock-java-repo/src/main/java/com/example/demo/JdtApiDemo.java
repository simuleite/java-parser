package com.example.demo;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.Expression;

/**
 * Demo: 验证 JDT API 能否正确解析各种边界case
 *
 * 验证目标:
 * 1. ClassInstanceCreation - new ArrayList<>()
 * 2. MethodInvocation 链式 - a.b().c()
 * 3. CastExpression - (String) obj
 * 4. MarkerAnnotation - @Deprecated
 */
public class JdtApiDemo {

    public static void main(String[] args) {
        System.out.println("=== JDT API 边界Case 验证 ===\n");

        // Test Case 1: 泛型实例化 - new ArrayList<>()
        testClassInstanceCreation();

        // Test Case 2: 方法链 - a.b().c()
        testMethodChain();

        // Test Case 3: 类型转换 - (String) obj
        testCastExpression();

        // Test Case 4: 注解 - @Deprecated
        testAnnotation();
    }

    /**
     * Test Case 1: ClassInstanceCreation - new ArrayList<>()
     */
    static void testClassInstanceCreation() {
        String code =
            "import java.util.List;\n" +
            "import java.util.ArrayList;\n" +
            "public class Test {\n" +
            "    public void test() {\n" +
            "        List<String> list = new ArrayList<>();\n" +
            "    }\n" +
            "}";

        System.out.println("=== Test 1: ClassInstanceCreation (new ArrayList<>()) ===");

        CompilationUnit cu = parse(code);

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(ClassInstanceCreation node) {
                System.out.println("  ✅ 捕获到 ClassInstanceCreation: new " + node.getType());
                System.out.println("    - 表达式: " + node.getExpression());
                System.out.println("    - 参数数量: " + node.arguments().size());

                // 获取类型绑定
                Type type = node.getType();
                System.out.println("    - 类型名称: " + type);

                return super.visit(node);
            }
        });

        System.out.println();
    }

    /**
     * Test Case 2: MethodInvocation 链式 - a.b().c()
     */
    static void testMethodChain() {
        String code =
            "public class Test {\n" +
            "    public void test() {\n" +
            "        String s = \"hello\";\n" +
            "        int len = s.trim().toLowerCase().length();\n" +
            "    }\n" +
            "}";

        System.out.println("=== Test 2: MethodInvocation 链式 (a.b().c()) ===");

        CompilationUnit cu = parse(code);

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                System.out.println("  ✅ 捕获到 MethodInvocation: " + node.getName());

                // 检查是否有 expression (方法链)
                Expression expr = node.getExpression();
                if (expr != null) {
                    System.out.println("    - expression: " + expr);
                    System.out.println("    - expression 类型: " + expr.getClass().getSimpleName());

                    // 递归检查
                    if (expr instanceof MethodInvocation) {
                        MethodInvocation mi = (MethodInvocation) expr;
                        System.out.println("    - 链式调用: " + mi.getName() + " 的 expression = " + mi.getExpression());
                    }
                } else {
                    System.out.println("    - 无 expression (顶层调用)");
                }

                return super.visit(node);
            }
        });

        System.out.println();
    }

    /**
     * Test Case 3: CastExpression - (String) obj
     */
    static void testCastExpression() {
        String code =
            "public class Test {\n" +
            "    public void test() {\n" +
            "        Object obj = \"test\";\n" +
            "        String str = (String) obj;\n" +
            "    }\n" +
            "}";

        System.out.println("=== Test 3: CastExpression ((String) obj) ===");

        CompilationUnit cu = parse(code);

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(CastExpression node) {
                System.out.println("  ✅ 捕获到 CastExpression");

                Type castType = node.getType();
                System.out.println("    - 转换类型: " + castType);
                System.out.println("    - 类型类: " + castType.getClass().getSimpleName());

                // 检查是否是 SimpleType
                if (castType instanceof org.eclipse.jdt.core.dom.SimpleType) {
                    org.eclipse.jdt.core.dom.SimpleType st = (org.eclipse.jdt.core.dom.SimpleType) castType;
                    System.out.println("    - SimpleType 名称: " + st.getName());
                }

                Expression expr = node.getExpression();
                System.out.println("    - 被转换表达式: " + expr);

                return super.visit(node);
            }
        });

        System.out.println();
    }

    /**
     * Test Case 4: MarkerAnnotation - @Deprecated
     */
    static void testAnnotation() {
        String code =
            "public class Test {\n" +
            "    @Deprecated\n" +
            "    public void test() {}\n" +
            "}";

        System.out.println("=== Test 4: MarkerAnnotation (@Deprecated) ===");

        CompilationUnit cu = parse(code);

        cu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MarkerAnnotation node) {
                System.out.println("  ✅ 捕获到 MarkerAnnotation: " + node.getTypeName());
                System.out.println("    - 完整名称: " + node.getTypeName().getFullyQualifiedName());

                return super.visit(node);
            }
        });

        System.out.println();
    }

    /**
     * 解析代码为 CompilationUnit
     */
    static CompilationUnit parse(String code) {
        ASTParser parser = ASTParser.newParser(AST.JLS17);
        parser.setSource(code.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        return cu;
    }
}
