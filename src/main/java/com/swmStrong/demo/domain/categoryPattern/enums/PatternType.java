package com.swmStrong.demo.domain.categoryPattern.enums;

import lombok.Getter;

@Getter
public enum PatternType {
    APP("app"),
    DOMAIN("domain");

    private final String value;

    PatternType(String value) {
        this.value = value;
    }

    public static PatternType fromValue(String value) {
        for (PatternType type : PatternType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid pattern type: " + value);
    }
}