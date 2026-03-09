package com.uniast.parser.builder;

import com.uniast.parser.helper.JavaModelHelper;
import com.uniast.parser.model.*;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IModuleBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import java.util.List;
import java.util.Set;

/**
 * 类型层次构建器
 * 负责构建类型继承关系和接口实现关联
 */
public class TypeHierarchyBuilder {

    private final JavaModelHelper modelHelper;
    private final boolean verbose;

    public TypeHierarchyBuilder() {
        this.modelHelper = new JavaModelHelper();
        this.verbose = false;
    }

    public TypeHierarchyBuilder(JavaModelHelper modelHelper, boolean verbose) {
        this.modelHelper = modelHelper;
        this.verbose = verbose;
    }

    /**
     * 构建类型层次结构
     * @param repo 仓库模型
     */
    public void build(Repository repo) {
        associateInterfaceImplementations(repo);
        associateSuperClassHierarchy(repo);
    }

    /**
     * 注册类型绑定（需要在解析时调用）
     */
    public void registerTypeBinding(String qualifiedName, ITypeBinding binding) {
        modelHelper.registerTypeBinding(qualifiedName, binding);
    }

    /**
     * 改进版：使用 JavaModelHelper 动态发现接口实现
     */
    private void associateInterfaceImplementations(Repository repo) {
        for (UniModule mod : repo.getModules().values()) {
            // Skip external modules
            if (mod.getDir() != null && !mod.getDir().isEmpty()) {
                continue;
            }

            for (UniPackage pkg : mod.getPackages().values()) {
                if (pkg.getTypes() == null) continue;

                for (UniType type : pkg.getTypes().values()) {
                    try {
                        // 查找 type binding
                        String qualifiedName = pkg.getPkgPath() + "." + type.getName();
                        ITypeBinding typeBinding = modelHelper.findTypeBinding(qualifiedName);

                        if (typeBinding != null) {
                            // 使用 JavaModelHelper 获取所有超级接口
                            Set<ITypeBinding> allSuperInterfaces =
                                modelHelper.getAllSuperInterfaces(typeBinding);

                            for (ITypeBinding superIface : allSuperInterfaces) {
                                // 添加到 type 的 implements 列表
                                Dependency ifaceDep = createDependency(superIface);
                                type.addImplement(ifaceDep);

                                // 反向关联：添加到 interface 的 implementations
                                Identity ifaceId = createIdentity(superIface);
                                UniType ifaceType = findType(repo, ifaceId);
                                if (ifaceType != null) {
                                    ifaceType.addImplementation(type.toIdentity());
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (verbose) {
                            System.err.println("Failed to build type hierarchy for " + type.getName() + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * 构建完整的超类层次结构
     */
    private void associateSuperClassHierarchy(Repository repo) {
        for (UniModule mod : repo.getModules().values()) {
            // Skip external modules
            if (mod.getDir() != null && !mod.getDir().isEmpty()) {
                continue;
            }

            for (UniPackage pkg : mod.getPackages().values()) {
                if (pkg.getTypes() == null) continue;

                for (UniType type : pkg.getTypes().values()) {
                    try {
                        // 查找 type binding
                        String qualifiedName = pkg.getPkgPath() + "." + type.getName();
                        ITypeBinding typeBinding = modelHelper.findTypeBinding(qualifiedName);

                        if (typeBinding != null) {
                            // 获取完整的超类链
                            List<ITypeBinding> superClassChain =
                                modelHelper.getSuperClassChain(typeBinding);

                            // 添加所有父类到 subStruct
                            for (ITypeBinding superClass : superClassChain) {
                                Dependency superDep = createDependency(superClass);
                                type.addSubStruct(superDep);
                            }
                        }
                    } catch (Exception e) {
                        if (verbose) {
                            System.err.println("Failed to build superclass hierarchy: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }

    /**
     * 从 ITypeBinding 创建 Identity
     */
    private Identity createIdentity(ITypeBinding typeBinding) {
        Identity id = new Identity();

        IModuleBinding module = typeBinding.getModule();
        if (module != null && module.getName() != null && !module.getName().isEmpty()) {
            id.setModPath(module.getName());
        } else if (typeBinding.isFromSource()) {
            id.setModPath("");
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
     * 从 ITypeBinding 创建 Dependency
     */
    private Dependency createDependency(ITypeBinding typeBinding) {
        Dependency dep = new Dependency();

        Identity id = createIdentity(typeBinding);
        dep.setModPath(id.getModPath());
        dep.setPkgPath(id.getPkgPath());
        dep.setName(id.getName());

        return dep;
    }

    /**
     * 推断库模块路径
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

        String[] parts = pkgName.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        } else if (parts.length == 1) {
            return parts[0];
        }

        return "java.base";
    }

    /**
     * 查找类型
     */
    private UniType findType(Repository repo, Identity typeId) {
        UniModule mod = repo.getModules().get(typeId.getModPath());
        if (mod == null) return null;

        UniPackage pkg = mod.getPackages().get(typeId.getPkgPath());
        if (pkg == null) return null;

        if (pkg.getTypes() == null) return null;
        return pkg.getTypes().get(typeId.getName());
    }
}
