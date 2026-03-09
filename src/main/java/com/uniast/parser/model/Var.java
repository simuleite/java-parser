package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Var represents a variable declaration.
 * Corresponds to uniast.Var in Go
 */
public class Var {
    @JsonProperty("IsExported")
    private boolean isExported;

    @JsonProperty("IsConst")
    private boolean isConst; // Java is always false

    @JsonProperty("IsPointer")
    private boolean isPointer; // Java is always false

    @JsonProperty("ModPath")
    private String modPath;

    @JsonProperty("PkgPath")
    private String pkgPath;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("File")
    private String file;

    @JsonProperty("Line")
    private int line;

    @JsonProperty("StartOffset")
    private int startOffset;

    @JsonProperty("EndOffset")
    private int endOffset;

    @JsonProperty("Type")
    private Identity type; // variable type

    @JsonProperty("Content")
    private String content;

    @JsonProperty("Dependencies")
    private List<Dependency> dependencies = new ArrayList<>();

    @JsonProperty("Groups")
    private List<Identity> groups = new ArrayList<>(); // for enum, Java is empty

    @JsonProperty("References")
    private List<Reference> references = new ArrayList<>();

    @JsonProperty("compress_data")
    private String compressData;

    public Var() {
        this.isConst = false;
        this.isPointer = false;
    }

    public boolean isExported() {
        return isExported;
    }

    public void setExported(boolean exported) {
        isExported = exported;
    }

    public boolean isConst() {
        return isConst;
    }

    public void setConst(boolean constVal) {
        isConst = constVal;
    }

    public boolean isPointer() {
        return isPointer;
    }

    public void setPointer(boolean pointer) {
        isPointer = pointer;
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

    public Identity getType() {
        return type;
    }

    public void setType(Identity type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Dependency> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(Dependency dep) {
        this.dependencies.add(dep);
    }

    public List<Identity> getGroups() {
        return groups;
    }

    public void setGroups(List<Identity> groups) {
        this.groups = groups;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    public void addReference(Identity refId) {
        Reference ref = new Reference();
        ref.setModPath(refId.getModPath());
        ref.setPkgPath(refId.getPkgPath());
        ref.setName(refId.getName());
        this.references.add(ref);
    }

    public String getCompressData() {
        return compressData;
    }

    public void setCompressData(String compressData) {
        this.compressData = compressData;
    }

    /**
     * Converts to Identity
     */
    public Identity toIdentity() {
        return new Identity(modPath, pkgPath, name);
    }
}
