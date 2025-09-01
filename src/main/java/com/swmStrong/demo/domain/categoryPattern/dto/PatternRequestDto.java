package com.swmStrong.demo.domain.categoryPattern.dto;

public record PatternRequestDto(
        String pattern
) {
    public static PatternRequestDto of(String pattern) {
        return new PatternRequestDto(pattern);
    }
}
