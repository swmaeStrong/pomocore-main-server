package com.swmStrong.demo.domain.categoryPattern.dto;

import com.swmStrong.demo.common.annotation.HexColor;

public record CategoryRequestDto(
        String category,
        Integer priority,
        @HexColor
        String color
) {
    public static CategoryRequestDto of(String category, Integer priority, String color) {
        return new CategoryRequestDto(category, priority, color);
    }
}
