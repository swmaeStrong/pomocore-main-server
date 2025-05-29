package com.swmStrong.demo.domain.categoryPattern.dto;

public record CategoryRequestDto(
        String category,
        Integer priority,
        String color
) {
    public static CategoryRequestDto of(String category, Integer priority, String color) {
        return new CategoryRequestDto(category, priority, color);
    }
}
