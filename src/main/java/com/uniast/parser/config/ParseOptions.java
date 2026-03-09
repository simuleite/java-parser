package com.uniast.parser.config;

/**
 * 解析选项
 */
public class ParseOptions {
    private boolean resolveBindings = true;
    private boolean verbose = false;
    private boolean excludeStandardLibraries = true;

    public boolean isResolveBindings() {
        return resolveBindings;
    }

    public void setResolveBindings(boolean resolveBindings) {
        this.resolveBindings = resolveBindings;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isExcludeStandardLibraries() {
        return excludeStandardLibraries;
    }

    public void setExcludeStandardLibraries(boolean excludeStandardLibraries) {
        this.excludeStandardLibraries = excludeStandardLibraries;
    }
}
