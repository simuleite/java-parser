package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Type represents a type declaration (class, interface, enum).
 * Corresponds to uniast.Type in Go
 */
public class UniType {
    @JsonProperty("Exported")
    private boolean exported;

    @JsonProperty("TypeKind")
    private TypeKind typeKind;

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

    // [新增] end line number (1-based)
    @JsonProperty("EndLine")
    private int endLine;

    @JsonProperty("Content")
    private String content;

    @JsonProperty("SubStruct")
    private List<Dependency> subStruct = new ArrayList<>(); // extends parent class

    @JsonProperty("InlineStruct")
    private List<Dependency> inlineStruct = new ArrayList<>(); // inherited fields, Java is empty

    @JsonProperty("Methods")
    private Map<String, Identity> methods = new HashMap<>(); // method name => Identity

    @JsonProperty("Implements")
    private List<Dependency> implementsList = new ArrayList<>(); // implemented interfaces

    @JsonProperty("Implementations")
    private List<Identity> implementations = new ArrayList<>(); // implementing classes

    @JsonProperty("References")
    private List<Reference> references = new ArrayList<>();

    @JsonProperty("compress_data")
    private String compressData;

    public UniType() {
    }

    public boolean isExported() {
        return exported;
    }

    public void setExported(boolean exported) {
        this.exported = exported;
    }

    public TypeKind getTypeKind() {
        return typeKind;
    }

    public void setTypeKind(TypeKind typeKind) {
        this.typeKind = typeKind;
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

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Dependency> getSubStruct() {
        return subStruct;
    }

    public void setSubStruct(List<Dependency> subStruct) {
        this.subStruct = subStruct;
    }

    public void addSubStruct(Dependency dep) {
        this.subStruct.add(dep);
    }

    public List<Dependency> getInlineStruct() {
        return inlineStruct;
    }

    public void setInlineStruct(List<Dependency> inlineStruct) {
        this.inlineStruct = inlineStruct;
    }

    public void addInlineStruct(Dependency dep) {
        this.inlineStruct.add(dep);
    }

    public Map<String, Identity> getMethods() {
        return methods;
    }

    public void setMethods(Map<String, Identity> methods) {
        this.methods = methods;
    }

    public void addMethod(String name, Identity identity) {
        this.methods.put(name, identity);
    }

    public List<Dependency> getImplements() {
        return implementsList;
    }

    public void setImplements(List<Dependency> implementsList) {
        this.implementsList = implementsList;
    }

    public void addImplement(Dependency dep) {
        this.implementsList.add(dep);
    }

    public List<Identity> getImplementations() {
        return implementations;
    }

    public void setImplementations(List<Identity> implementations) {
        this.implementations = implementations;
    }

    public void addImplementation(Identity implId) {
        this.implementations.add(implId);
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
