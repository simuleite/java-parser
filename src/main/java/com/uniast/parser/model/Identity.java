package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Identity represents the universal-unique for an ast node.
 * Corresponds to uniast.Identity in Go
 */
public class Identity {
    @JsonProperty("ModPath")
    private String modPath; // module id, format: {ModName} or {ModName}@{Version}

    @JsonProperty("PkgPath")
    private String pkgPath; // namespace of the ast node

    @JsonProperty("Name")
    private String name; // unique name of the ast node

    public Identity() {
    }

    public Identity(String modPath, String pkgPath, String name) {
        this.modPath = modPath;
        this.pkgPath = pkgPath;
        this.name = name;
    }

    public String getModPath() {
        return modPath;
    }

    public void setModPath(String modPath) {
        this.modPath = modPath;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns full packagepath.name
     * Corresponds to Identity.String() in Go
     */
    public String toString() {
        return pkgPath + "#" + name;
    }

    /**
     * Creates full identity string with mod path
     * Format: modPath?pkgPath#name
     */
    public String fullString() {
        return modPath + "?" + pkgPath + "#" + name;
    }
}
