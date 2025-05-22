package com.swmStrong.demo.domain.categoryPattern.dto;

import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;

import java.util.List;

public record CategoryResponseDto(
        String category,
        String color,
        List<String> patterns
) {
    public static CategoryResponseDto from(CategoryPattern categoryPattern) {
        return new CategoryResponseDto(
                categoryPattern.getCategory(),
                categoryPattern.getColor(),
                categoryPattern.getPatterns()
        );
    }
}
