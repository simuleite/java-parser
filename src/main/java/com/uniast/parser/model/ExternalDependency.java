package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.uniast.parser.helper.JavaModelHelper;

/**
 * ExternalDependency represents a dependency that is not part of the current repository.
 * This includes:
 * - Java standard library (java.*, javax.*, etc.)
 * - Third-party libraries from Maven dependencies
 * - External modules not in the current project
 *
 * Unlike regular Dependency, ExternalDependency is explicitly marked as external
 * to distinguish it from internal cross-package references.
 */
public class ExternalDependency extends Dependency {

    /**
     * Indicates whether this is from Java standard library
     */
    @JsonProperty("IsStandardLibrary")
    private boolean isStandardLibrary;

    /**
     * External library version (if available from Maven)
     */
    @JsonProperty("LibraryVersion")
    private String libraryVersion;

    public ExternalDependency() {
        super();
    }

    public ExternalDependency(String modPath, String pkgPath, String name) {
        super();
        this.setModPath(modPath);
        this.setPkgPath(pkgPath);
        this.setName(name);
    }

    public boolean isStandardLibrary() {
        return isStandardLibrary;
    }

    public void setStandardLibrary(boolean standardLibrary) {
        isStandardLibrary = standardLibrary;
    }

    public String getLibraryVersion() {
        return libraryVersion;
    }

    public void setLibraryVersion(String libraryVersion) {
        this.libraryVersion = libraryVersion;
    }

    /**
     * Factory method to create ExternalDependency from ITypeBinding
     */
    public static ExternalDependency fromTypeBinding(
            org.eclipse.jdt.core.dom.ITypeBinding typeBinding,
            JavaModelHelper modelHelper) {
        ExternalDependency extDep = new ExternalDependency();

        // Set module path
        org.eclipse.jdt.core.dom.IModuleBinding module = typeBinding.getModule();
        if (module != null && module.getName() != null && !module.getName().isEmpty()) {
            extDep.setModPath(module.getName());
        } else {
            extDep.setModPath("java.base");
        }

        // Set package path
        org.eclipse.jdt.core.dom.IPackageBinding pkg = typeBinding.getPackage();
        if (pkg != null && pkg.getName() != null && !pkg.getName().isEmpty()) {
            extDep.setPkgPath(pkg.getName());
        } else {
            extDep.setPkgPath("");
        }

        // Set name (simple name)
        extDep.setName(typeBinding.getName());

        // Set standard library flag
        extDep.setStandardLibrary(modelHelper.isJavaStandardLibrary(typeBinding));

        return extDep;
    }

    /**
     * Factory method to create ExternalDependency from Identity
     */
    public static ExternalDependency fromIdentity(Identity identity) {
        ExternalDependency extDep = new ExternalDependency();
        extDep.setModPath(identity.getModPath());
        extDep.setPkgPath(identity.getPkgPath());
        extDep.setName(identity.getName());
        return extDep;
    }
}
