package com.uniast.parser.helper;

import org.eclipse.jdt.core.dom.ITypeBinding;
import java.util.Set;
import java.util.List;

/**
 * 类型系统辅助接口
 */
public interface ITypeSystemHelper {
    /**
     * 注册类型绑定
     * @param qualifiedName 全限定名
     * @param binding JDT 类型绑定
     */
    void registerTypeBinding(String qualifiedName, ITypeBinding binding);

    /**
     * 查找类型绑定
     * @param name 全限定名或简单名
     * @return ITypeBinding 或 null
     */
    ITypeBinding findTypeBinding(String name);

    /**
     * 获取所有超级接口（包括间接接口）
     * @param typeBinding 类型绑定
     * @return 超级接口集合
     */
    Set<ITypeBinding> getAllSuperInterfaces(ITypeBinding typeBinding);

    /**
     * 获取完整的超类链
     * @param typeBinding 类型绑定
     * @return 超类列表（最近的在前）
     */
    List<ITypeBinding> getSuperClassChain(ITypeBinding typeBinding);

    /**
     * 检查是否实现接口
     * @param subType 子类型
     * @param iface 接口
     * @return 是否实现
     */
    boolean implementsInterface(ITypeBinding subType, ITypeBinding iface);

    /**
     * 检查是否为子类型
     * @param subType 子类型
     * @param superType 父类型
     * @return 是否为子类型
     */
    boolean isSubType(ITypeBinding subType, ITypeBinding superType);

    /**
     * 检查是否为 Java 标准库
     * @param typeBinding 类型绑定
     * @return 是否为标准库
     */
    boolean isJavaStandardLibrary(ITypeBinding typeBinding);

    /**
     * 检查是否为外部依赖
     * @param typeBinding 类型绑定
     * @param modulePath 模块路径
     * @return 是否为外部依赖
     */
    boolean isExternalDependency(ITypeBinding typeBinding, String modulePath);
}
