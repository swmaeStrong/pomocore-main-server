package com.swmStrong.demo.domain.categoryPattern.dto;

public record CategoryRequestDto(
        String category,
        Integer priority
) {
    public static CategoryRequestDto of(String category, Integer priority) {
        return new CategoryRequestDto(category, priority);
    }
}
