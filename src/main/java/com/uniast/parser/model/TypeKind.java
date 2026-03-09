package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * TypeKind represents the kind of type.
 * Corresponds to uniast.TypeKind in Go
 */
public enum TypeKind {
    STRUCT("struct"),
    INTERFACE("interface"),
    TYPEDEF("typedef"),
    ENUM("enum");

    private final String value;

    TypeKind(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
