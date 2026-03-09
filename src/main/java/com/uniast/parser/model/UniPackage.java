package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Package represents a Go package or Java package.
 * Corresponds to uniast.Package in Go
 */
public class UniPackage {
    @JsonProperty("IsMain")
    private boolean isMain;

    @JsonProperty("IsTest")
    private boolean isTest;

    @JsonProperty("PkgPath")
    private String pkgPath;

    @JsonProperty("Functions")
    private Map<String, Function> functions = new HashMap<>();

    @JsonProperty("Types")
    private Map<String, UniType> types = new HashMap<>();

    @JsonProperty("Vars")
    private Map<String, Var> vars = new HashMap<>();

    @JsonProperty("compress_data")
    private String compressData;

    public UniPackage() {
        this.isMain = true; // Java is always main
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    public String getPkgPath() {
        return pkgPath;
    }

    public void setPkgPath(String pkgPath) {
        this.pkgPath = pkgPath;
    }

    public Map<String, Function> getFunctions() {
        return functions;
    }

    public void setFunctions(Map<String, Function> functions) {
        this.functions = functions;
    }

    public void addFunction(String name, Function function) {
        this.functions.put(name, function);
    }

    public Map<String, UniType> getTypes() {
        return types;
    }

    public void setTypes(Map<String, UniType> types) {
        this.types = types;
    }

    public void addType(String name, UniType type) {
        this.types.put(name, type);
    }

    public Map<String, Var> getVars() {
        return vars;
    }

    public void setVars(Map<String, Var> vars) {
        this.vars = vars;
    }

    public void addVar(String name, Var var) {
        this.vars.put(name, var);
    }

    public String getCompressData() {
        return compressData;
    }

    public void setCompressData(String compressData) {
        this.compressData = compressData;
    }
}
