package com.uniast.parser.resolver;

import java.util.List;

/**
 * 依赖解析结果封装
 */
public class DependencyResult {
    private String groupId;
    private String artifactId;
    private String version;
    private List<String> jarPaths;

    public DependencyResult() {}

    public DependencyResult(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public DependencyResult(String groupId, String artifactId, String version, List<String> jarPaths) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.jarPaths = jarPaths;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getJarPaths() {
        return jarPaths;
    }

    public void setJarPaths(List<String> jarPaths) {
        this.jarPaths = jarPaths;
    }

    /**
     * 返回完整的模块标识：groupId:artifactId:version
     */
    public String toModulePath() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
