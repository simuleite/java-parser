package com.uniast.parser.parser;

import java.util.List;
import java.io.IOException;

public class TestSymbolExtractor {
    public static void main(String[] args) throws IOException {
        System.out.println("=== 测试 AstParserFacade.java ===\n");
        
        List<SymbolExtractorDemo.SymbolInfo> symbols = 
            SymbolExtractorDemo.extractTopLevelSymbols(
                "src/main/java/com/uniast/parser/parser/AstParserFacade.java"
            );
        
        System.out.println("【TYPE】");
        symbols.stream()
            .filter(s -> "TYPE".equals(s.type))
            .forEach(System.out::println);
        
        System.out.println("\n【VAR 字段】");
        symbols.stream()
            .filter(s -> "VAR".equals(s.type))
            .forEach(System.out::println);
        
        System.out.println("\n【FUNC 方法】(仅显示名称)");
        symbols.stream()
            .filter(s -> "FUNC".equals(s.type))
            .forEach(s -> System.out.println("  " + s.name + " (line " + s.line + ")"));
        
        System.out.println("\n统计:");
        System.out.println("  TYPE: " + symbols.stream().filter(s -> "TYPE".equals(s.type)).count());
        System.out.println("  VAR: " + symbols.stream().filter(s -> "VAR".equals(s.type)).count());
        System.out.println("  FUNC: " + symbols.stream().filter(s -> "FUNC".equals(s.type)).count());
        
        // 验证局部变量
        String[] locals = {"repoPathAbs", "javaFiles", "conv", "analyzer", "repo", "uniModule", "graphBuilder"};
        boolean hasLocal = false;
        for (String var : locals) {
            if (symbols.stream().anyMatch(s -> s.name.endsWith("." + var))) {
                System.out.println("\n❌ 发现局部变量: " + var);
                hasLocal = true;
            }
        }
        if (!hasLocal) {
            System.out.println("\n✅ 无局部变量混入");
        }
    }
}
