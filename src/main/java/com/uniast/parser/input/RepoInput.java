package com.uniast.parser.input;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 仓库输入封装
 */
public class RepoInput {
    private String path;
    private String moduleId;
    private String moduleVersion;
    private Path pomFile;

    public RepoInput(String path) {
        this.path = Paths.get(path).toAbsolutePath().normalize().toString();
    }

    public RepoInput(Path path, String moduleName, String moduleVersion) {
        this.path = path.toAbsolutePath().normalize().toString();
        this.moduleId = moduleName;
        this.moduleVersion = moduleVersion;
    }

    public Path getRepoPath() {
        return Paths.get(path);
    }

    public String getPath() {
        return path;
    }

    public String getModuleName() {
        return moduleId;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public Path getPomFile() {
        if (pomFile == null) {
            pomFile = Paths.get(path, "pom.xml");
        }
        return pomFile;
    }

    public void setPomFile(Path pomFile) {
        this.pomFile = pomFile;
    }
}
