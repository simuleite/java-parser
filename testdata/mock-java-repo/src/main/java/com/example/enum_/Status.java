package com.example.enum_;

/**
 * 枚举类型 - 测试枚举依赖捕获
 */
public enum Status {
    ACTIVE,
    INACTIVE,
    PENDING;

    public static Status fromString(String s) {
        return valueOf(s.toUpperCase());
    }
}
