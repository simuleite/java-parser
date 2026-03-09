package com.uniast.parser.model;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Language represents the programming language.
 * Corresponds to uniast.Language in Go
 */
public enum Language {
    GOLANG("go"),
    RUST("rust"),
    CXX("cxx"),
    PYTHON("python"),
    TYPESCRIPT("typescript"),
    JAVA("java"),
    KOTLIN("kotlin"),
    UNKNOWN("");

    private final String value;

    Language(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static Language fromString(String lang) {
        if (lang == null) {
            return UNKNOWN;
        }
        switch (lang.toLowerCase()) {
            case "go":
            case "golang":
                return GOLANG;
            case "rust":
                return RUST;
            case "cxx":
            case "c++":
                return CXX;
            case "python":
                return PYTHON;
            case "ts":
            case "typescript":
            case "javascript":
            case "js":
                return TYPESCRIPT;
            case "java":
                return JAVA;
            case "kotlin":
                return KOTLIN;
            default:
                return UNKNOWN;
        }
    }
}
