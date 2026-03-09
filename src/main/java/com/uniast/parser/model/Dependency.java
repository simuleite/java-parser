package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Dependency represents a relation to another node.
 * STRICTLY ALIGNED with uniast.proto Relation message.
 *
 * Proto definition:
 *   message Relation {
 *     string kind = 1;
 *     string ModPath = 6;
 *     string PkgPath = 7;
 *     string Name = 8;
 *     int32 line = 3;
 *     string desc = 4;
 *     string codes = 5;
 *   }
 */
public class Dependency {
    @JsonProperty("Kind")
    private String kind;

    @JsonProperty("ModPath")
    private String modPath;

    @JsonProperty("PkgPath")
    private String pkgPath;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Line")
    private Integer line;

    @JsonProperty("Desc")
    private String desc;

    @JsonProperty("Codes")
    private String codes;

    public Dependency() {
    }

    public Dependency(Identity identity) {
        this.modPath = identity.getModPath();
        this.pkgPath = identity.getPkgPath();
        this.name = identity.getName();
    }

    public Dependency(Identity identity, int line) {
        this.modPath = identity.getModPath();
        this.pkgPath = identity.getPkgPath();
        this.name = identity.getName();
        this.line = line;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
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

    public Integer getLine() {
        return line;
    }

    public void setLine(Integer line) {
        this.line = line;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCodes() {
        return codes;
    }

    public void setCodes(String codes) {
        this.codes = codes;
    }

    /**
     * Converts to Identity
     */
    public Identity toIdentity() {
        return new Identity(modPath, pkgPath, name);
    }
}
