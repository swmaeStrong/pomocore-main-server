package com.swmStrong.demo.domain.categoryPattern.dto;

public record CategoryRequestDto(
        String category,
        String color
) {
    public static CategoryRequestDto of(String category, String color) {
        return new CategoryRequestDto(category, color);
    }
}
