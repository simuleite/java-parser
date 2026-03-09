package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * FileLine represents a filename and line number.
 * Corresponds to uniast.FileLine in Go
 */
public class FileLine {
    @JsonProperty("File")
    private String file;

    @JsonProperty("Line")
    private int line; // start line number (1-based)

    @JsonProperty("EndLine")
    private int endLine; // end line number (1-based)

    @JsonProperty("StartOffset")
    private int startOffset; // start byte offset in file

    @JsonProperty("EndOffset")
    private int endOffset; // end byte offset in file

    public FileLine() {
    }

    public FileLine(String file, int line, int startOffset, int endOffset) {
        this.file = file;
        this.line = line;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(int startOffset) {
        this.startOffset = startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(int endOffset) {
        this.endOffset = endOffset;
    }
}
