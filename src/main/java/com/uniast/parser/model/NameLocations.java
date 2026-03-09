package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * NameLocations represents all locations of a symbol name.
 * Corresponds to uniast.NameLocations in Go (for reverse index name → files).
 */
public class NameLocations {
    @JsonProperty("Files")
    private List<String> files = new ArrayList<>();

    public NameLocations() {
    }

    public NameLocations(List<String> files) {
        this.files = files;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public void addFile(String file) {
        this.files.add(file);
    }
}
