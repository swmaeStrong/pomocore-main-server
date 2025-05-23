package com.swmStrong.demo.domain.categoryPattern.dto;

import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;

import java.util.Set;

public record CategoryResponseDto(
        String category,
        String color,
        Set<String> patterns
) {
    public static CategoryResponseDto from(CategoryPattern categoryPattern) {
        return new CategoryResponseDto(
                categoryPattern.getCategory(),
                categoryPattern.getColor(),
                categoryPattern.getPatterns()
        );
    }
}
