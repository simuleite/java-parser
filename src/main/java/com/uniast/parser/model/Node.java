package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Node represents an AST node with its complete information.
 * Corresponds to uniast.Node in Go
 */
public class Node {
    @JsonProperty("ModPath")
    private String modPath;

    @JsonProperty("PkgPath")
    private String pkgPath;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Type")
    private String type; // node type: TYPE, FUNCTION, VAR

    @JsonProperty("File")
    private String file;

    @JsonProperty("Line")
    private int line;

    @JsonProperty("StartOffset")
    private int startOffset;

    @JsonProperty("EndOffset")
    private int endOffset;

    @JsonProperty("Codes")
    private String codes; // source code content

    @JsonProperty("Description")
    private String description;

    @JsonProperty("Dependencies")
    private Dependency[] dependencies;

    @JsonProperty("References")
    private Dependency[] references;

    public Node() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }

    public String getCodes() {
        return codes;
    }

    public void setCodes(String codes) {
        this.codes = codes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Dependency[] getDependencies() {
        return dependencies;
    }

    public void setDependencies(Dependency[] dependencies) {
        this.dependencies = dependencies;
    }

    public Dependency[] getReferences() {
        return references;
    }

    public void setReferences(Dependency[] references) {
        this.references = references;
    }

    /**
     * Gets the Identity of this node
     */
    public Identity getIdentity() {
        return new Identity(modPath, pkgPath, name);
    }

    /**
     * Converts to Identity
     */
    public Identity toIdentity() {
        return new Identity(modPath, pkgPath, name);
    }
}
