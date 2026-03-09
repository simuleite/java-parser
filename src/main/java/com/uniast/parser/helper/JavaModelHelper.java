package com.uniast.parser.helper;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 类型系统辅助实现
 * 使用 JDT ITypeBinding 提供类型层次和兼容性检查
 */
public class JavaModelHelper implements ITypeSystemHelper {

    private final Map<String, ITypeBinding> typeBindingsCache;

    public JavaModelHelper() {
        this.typeBindingsCache = new HashMap<>();
    }

    /**
     * 注册类型绑定
     * @param qualifiedName 全限定名
     * @param binding JDT 类型绑定
     */
    @Override
    public void registerTypeBinding(String qualifiedName, ITypeBinding binding) {
        typeBindingsCache.put(qualifiedName, binding);
        // Also register with simple name for convenience
        typeBindingsCache.put(binding.getName(), binding);
    }

    /**
     * 查找类型绑定
     * @param name 全限定名或简单名
     * @return ITypeBinding 或 null
     */
    @Override
    public ITypeBinding findTypeBinding(String name) {
        return typeBindingsCache.get(name);
    }

    /**
     * 获取所有超级接口（包括间接接口）
     * @param typeBinding 类型绑定
     * @return 超级接口集合
     */
    @Override
    public Set<ITypeBinding> getAllSuperInterfaces(ITypeBinding typeBinding) {
        Set<ITypeBinding> allInterfaces = new HashSet<>();
        collectAllSuperInterfaces(typeBinding, allInterfaces);
        return allInterfaces;
    }

    /**
     * 递归收集所有超级接口
     */
    private void collectAllSuperInterfaces(ITypeBinding typeBinding, Set<ITypeBinding> collected) {
        if (typeBinding == null || typeBinding.isPrimitive() || typeBinding.isArray()) {
            return;
        }

        // Get direct interfaces
        ITypeBinding[] interfaces = typeBinding.getInterfaces();
        for (ITypeBinding iface : interfaces) {
            if (iface != null && collected.add(iface)) {
                // Recursively collect super interfaces
                collectAllSuperInterfaces(iface, collected);
            }
        }

        // Also check superclass (it might implement interfaces)
        ITypeBinding superClass = typeBinding.getSuperclass();
        if (superClass != null && !superClass.getQualifiedName().equals("java.lang.Object")) {
            collectAllSuperInterfaces(superClass, collected);
        }
    }

    /**
     * 获取完整的超类链
     * @param typeBinding 类型绑定
     * @return 超类列表（最近的在前）
     */
    @Override
    public List<ITypeBinding> getSuperClassChain(ITypeBinding typeBinding) {
        List<ITypeBinding> chain = new java.util.ArrayList<>();
        ITypeBinding current = typeBinding.getSuperclass();

        while (current != null && !current.getQualifiedName().equals("java.lang.Object")) {
            chain.add(current);
            current = current.getSuperclass();
        }

        return chain;
    }

    /**
     * 检查是否实现接口
     * @param subTypeBinding 子类型
     * @param iface 接口
     * @return 是否实现
     */
    @Override
    public boolean implementsInterface(ITypeBinding subTypeBinding, ITypeBinding iface) {
        if (subTypeBinding == null || iface == null) {
            return false;
        }

        if (!iface.isInterface()) {
            return false;
        }

        Set<ITypeBinding> allInterfaces = getAllSuperInterfaces(subTypeBinding);
        return allInterfaces.contains(iface);
    }

    /**
     * 检查是否为子类型
     * @param subTypeBinding 子类型
     * @param superTypeBinding 父类型
     * @return 是否为子类型
     */
    @Override
    public boolean isSubType(ITypeBinding subTypeBinding, ITypeBinding superTypeBinding) {
        if (subTypeBinding == null || superTypeBinding == null) {
            return false;
        }

        // Check if it's the same type
        if (subTypeBinding.equals(superTypeBinding)) {
            return true;
        }

        // Check if it implements the interface
        if (superTypeBinding.isInterface()) {
            return implementsInterface(subTypeBinding, superTypeBinding);
        }

        // Check superclass chain
        ITypeBinding current = subTypeBinding.getSuperclass();
        while (current != null && !current.getQualifiedName().equals("java.lang.Object")) {
            if (current.equals(superTypeBinding)) {
                return true;
            }
            current = current.getSuperclass();
        }

        return false;
    }

    /**
     * 检查是否为 Java 标准库
     * @param typeBinding 类型绑定
     * @return 是否为标准库
     */
    @Override
    public boolean isJavaStandardLibrary(ITypeBinding typeBinding) {
        if (typeBinding == null) {
            return false;
        }

        String packageName = typeBinding.getPackage() != null
            ? typeBinding.getPackage().getName()
            : "";

        return packageName.startsWith("java.")
            || packageName.startsWith("javax.")
            || packageName.startsWith("org.omg.")
            || packageName.startsWith("org.w3c.")
            || packageName.startsWith("org.xml.");
    }

    /**
     * 检查是否为外部依赖
     * @param typeBinding 类型绑定
     * @param modulePath 模块路径
     * @return 是否为外部依赖
     */
    @Override
    public boolean isExternalDependency(ITypeBinding typeBinding, String modulePath) {
        if (typeBinding == null) {
            return false;
        }

        // If it's from Java standard library, it's external
        if (isJavaStandardLibrary(typeBinding)) {
            return true;
        }

        // Check if it's from a different module
        IModuleBinding module = typeBinding.getModule();
        if (module != null && !module.getName().isEmpty()) {
            return !module.getName().equals(modulePath);
        }

        return false;
    }
}
