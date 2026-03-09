package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Reference represents a reference to another node.
 * Corresponds to uniast.Relation in Go
 */
public class Reference {
    @JsonProperty("ModPath")
    private String modPath;

    @JsonProperty("PkgPath")
    private String pkgPath;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Line")
    private int line;

    public Reference() {
    }

    public Reference(String modPath, String pkgPath, String name) {
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

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }
}
