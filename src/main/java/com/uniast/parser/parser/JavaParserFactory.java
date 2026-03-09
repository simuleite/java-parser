package com.uniast.parser.parser;

import com.uniast.parser.input.RepoInput;
import com.uniast.parser.config.ParseOptions;

/**
 * Java 解析器工厂
 * 提供静态工厂方法创建 IJavaParser 实例
 */
public class JavaParserFactory {

    /**
     * 创建默认配置的解析器
     * @return IJavaParser 实例
     */
    public static IJavaParser createParser() {
        return new AstParserFacade();
    }

    /**
     * 根据输入和选项创建解析器
     * @param input 仓库输入
     * @param options 解析选项
     * @return IJavaParser 实例
     */
    public static IJavaParser createParser(RepoInput input, ParseOptions options) {
        String moduleName = input.getModuleName();
        boolean resolveBindings = options.isResolveBindings();
        boolean verbose = options.isVerbose();
        boolean excludeStandardLibraries = options.isExcludeStandardLibraries();

        return new AstParserFacade(moduleName, resolveBindings, verbose, excludeStandardLibraries);
    }

    /**
     * 根据参数创建解析器
     * @param moduleName 模块名称
     * @param resolveBindings 是否解析类型绑定
     * @param verbose 是否输出详细日志
     * @param excludeStandardLibraries 是否排除标准库
     * @return IJavaParser 实例
     */
    public static IJavaParser createParser(String moduleName, boolean resolveBindings,
                                          boolean verbose, boolean excludeStandardLibraries) {
        return new AstParserFacade(moduleName, resolveBindings, verbose, excludeStandardLibraries);
    }
}
