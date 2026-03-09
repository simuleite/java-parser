package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * File represents a source file.
 * Corresponds to uniast.File in proto
 */
public class UniFile {
    @JsonProperty("Path")
    private String path;

    @JsonProperty("Imports")
    private List<Import> imports = new ArrayList<>();

    // Identity fields (matching Module/Package hierarchy)
    @JsonProperty("ModPath")
    private String modPath;

    @JsonProperty("PkgPath")
    private String pkgPath;

    // Lightweight symbol name index (reference only, full data in Package)
    // Accelerates get_file_structure API
    @JsonProperty("FunctionNames")
    private List<String> functionNames = new ArrayList<>();

    @JsonProperty("TypeNames")
    private List<String> typeNames = new ArrayList<>();

    @JsonProperty("VarNames")
    private List<String> varNames = new ArrayList<>();

    public UniFile() {
    }

    public UniFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<Import> getImports() {
        return imports;
    }

    public void setImports(List<Import> imports) {
        this.imports = imports;
    }

    public void addImport(Import importDecl) {
        this.imports.add(importDecl);
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

    public List<String> getFunctionNames() {
        return functionNames;
    }

    public void setFunctionNames(List<String> functionNames) {
        this.functionNames = functionNames;
    }

    public void addFunctionName(String name) {
        this.functionNames.add(name);
    }

    public List<String> getTypeNames() {
        return typeNames;
    }

    public void setTypeNames(List<String> typeNames) {
        this.typeNames = typeNames;
    }

    public void addTypeName(String name) {
        this.typeNames.add(name);
    }

    public List<String> getVarNames() {
        return varNames;
    }

    public void setVarNames(List<String> varNames) {
        this.varNames = varNames;
    }

    public void addVarName(String name) {
        this.varNames.add(name);
    }
}
