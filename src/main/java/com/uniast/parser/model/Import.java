package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Import represents an import declaration.
 * Corresponds to uniast.Import in Go
 */
public class Import {
    @JsonProperty("Alias")
    private String alias; // optional, can be null

    @JsonProperty("Path")
    private String path;

    public Import() {
    }

    public Import(String path) {
        this.path = path;
    }

    public Import(String alias, String path) {
        this.alias = alias;
        this.path = path;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
