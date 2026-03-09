package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Module represents a module (Go module or Java project).
 * Corresponds to uniast.Module in Go
 */
public class UniModule {
    @JsonProperty("Language")
    private Language language;

    @JsonProperty("Version")
    private String version;

    @JsonProperty("Name")
    private String name; // module name (e.g., "com.example:app:1.0.0")

    @JsonProperty("Dir")
    private String dir; // relative path to repo

    @JsonProperty("Packages")
    private Map<String, UniPackage> packages = new HashMap<>();

    @JsonProperty("Dependencies")
    private Map<String, String> dependencies = new HashMap<>(); // module name => module_path@version

    @JsonProperty("Files")
    private Map<String, UniFile> files = new HashMap<>();

    @JsonProperty("compress_data")
    private String compressData;

    public UniModule() {
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public Map<String, UniPackage> getPackages() {
        return packages;
    }

    public void setPackages(Map<String, UniPackage> packages) {
        this.packages = packages;
    }

    public void addPackage(String pkgPath, UniPackage pkg) {
        this.packages.put(pkgPath, pkg);
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies;
    }

    public void addDependency(String name, String version) {
        this.dependencies.put(name, version);
    }

    public Map<String, UniFile> getFiles() {
        return files;
    }

    public void setFiles(Map<String, UniFile> files) {
        this.files = files;
    }

    public void addFile(String path, UniFile uniFile) {
        this.files.put(path, uniFile);
    }

    public String getCompressData() {
        return compressData;
    }

    public void setCompressData(String compressData) {
        this.compressData = compressData;
    }
}
