package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository represents a code repository with complete AST information.
 * Corresponds to uniast.Repository in Go
 */
public class Repository {
    @JsonProperty("id")
    private String id; // repository name

    @JsonProperty("ASTVersion")
    private String ASTVersion; // UniAST version (e.g., "v0.4.0")

    @JsonProperty("ToolVersion")
    private String ToolVersion; // reni-java-parser version

    @JsonProperty("Path")
    private String Path; // repository absolute path

    @JsonProperty("RepoVersion")
    private RepoVersion repoVersion; // Git version info (optional)

    @JsonProperty("Modules")
    private Map<String, UniModule> modules = new HashMap<>();

    @JsonProperty("Graph")
    private Map<String, Node> graph = new HashMap<>();

    // [新增] name → files 反向索引，加速 search_symbol API，无需独立 .idx 文件
    @JsonProperty("NameToLocations")
    private Map<String, NameLocations> nameToLocations = new HashMap<>();

    /**
     * External dependencies (JDK, third-party libraries, etc.)
     * Unlike internal dependencies, these are not part of the current repository
     */
    @JsonProperty("ExternalDependencies")
    private List<ExternalDependency> externalDependencies = new ArrayList<>();

    public Repository() {
        this.ASTVersion = "v0.1.4";  // Match reference file version
        this.ToolVersion = "reni-java-parser v1.0.0";
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("ASTVersion")
    public String getASTVersion() {
        return ASTVersion;
    }

    @JsonProperty("ASTVersion")
    public void setASTVersion(String ASTVersion) {
        this.ASTVersion = ASTVersion;
    }

    @JsonProperty("ToolVersion")
    public String getToolVersion() {
        return ToolVersion;
    }

    @JsonProperty("ToolVersion")
    public void setToolVersion(String ToolVersion) {
        this.ToolVersion = ToolVersion;
    }

    @JsonProperty("Path")
    public String getPath() {
        return Path;
    }

    @JsonProperty("Path")
    public void setPath(String path) {
        Path = path;
    }

    public RepoVersion getRepoVersion() {
        return repoVersion;
    }

    public void setRepoVersion(RepoVersion repoVersion) {
        this.repoVersion = repoVersion;
    }

    public Map<String, UniModule> getModules() {
        return modules;
    }

    public void setModules(Map<String, UniModule> modules) {
        this.modules = modules;
    }

    public void addModule(String name, UniModule uniModule) {
        this.modules.put(name, uniModule);
    }

    public Map<String, Node> getGraph() {
        return graph;
    }

    public void setGraph(Map<String, Node> graph) {
        this.graph = graph;
    }

    public void addNode(String id, Node node) {
        this.graph.put(id, node);
    }

    public Map<String, NameLocations> getNameToLocations() {
        return nameToLocations;
    }

    public void setNameToLocations(Map<String, NameLocations> nameToLocations) {
        this.nameToLocations = nameToLocations;
    }

    public void addNameLocation(String name, String file) {
        NameLocations locations = nameToLocations.computeIfAbsent(name, k -> new NameLocations());
        locations.addFile(file);
    }

    public List<ExternalDependency> getExternalDependencies() {
        return externalDependencies;
    }

    public void setExternalDependencies(List<ExternalDependency> externalDependencies) {
        this.externalDependencies = externalDependencies;
    }

    public void addExternalDependency(ExternalDependency extDep) {
        if (!this.externalDependencies.contains(extDep)) {
            this.externalDependencies.add(extDep);
        }
    }
}

