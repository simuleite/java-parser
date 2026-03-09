package com.uniast.parser.config;

/**
 * CLI 参数封装
 */
public class CliOptions {
    private String repoPath;
    private String outputPath;
    private boolean noBindings;
    private boolean verbose;
    private boolean includeStandardLibraries;
    private boolean skipMaven;
    private String moduleName;
    private String moduleVersion;
    private int threads = 2;

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public boolean isNoBindings() {
        return noBindings;
    }

    public void setNoBindings(boolean noBindings) {
        this.noBindings = noBindings;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isIncludeStandardLibraries() {
        return includeStandardLibraries;
    }

    public void setIncludeStandardLibraries(boolean includeStandardLibraries) {
        this.includeStandardLibraries = includeStandardLibraries;
    }

    public boolean isSkipMaven() {
        return skipMaven;
    }

    public void setSkipMaven(boolean skipMaven) {
        this.skipMaven = skipMaven;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    /**
     * 转换为 ParseOptions
     */
    public ParseOptions toParseOptions() {
        ParseOptions options = new ParseOptions();
        options.setResolveBindings(!noBindings);
        options.setVerbose(verbose);
        options.setExcludeStandardLibraries(!includeStandardLibraries);
        return options;
    }
}
