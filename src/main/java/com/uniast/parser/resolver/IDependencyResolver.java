package com.uniast.parser.resolver;

import java.nio.file.Path;
import java.util.List;

/**
 * 依赖解析器接口
 */
public interface IDependencyResolver {
    /**
     * 解析依赖
     * @param pomFile pom.xml 路径
     * @return 依赖 JAR 路径列表
     */
    List<String> resolve(Path pomFile) throws Exception;

    /**
     * 提取模块信息
     * @param pomFile pom.xml 路径
     * @return 模块信息
     */
    DependencyResult extractModuleInfo(Path pomFile) throws Exception;

    /**
     * 简单提取模块信息（不使用完整 Maven 模型）
     * @param pomFile pom.xml 路径
     * @return 模块信息
     */
    DependencyResult extractModuleInfoSimple(Path pomFile) throws Exception;

    /**
     * 解析 Monorepo 模块列表
     * @param pomFile parent pom.xml
     * @return 模块名称列表
     */
    List<String> parseModules(Path pomFile);
}
