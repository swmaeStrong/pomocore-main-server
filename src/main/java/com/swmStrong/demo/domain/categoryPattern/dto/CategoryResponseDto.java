package com.swmStrong.demo.domain.categoryPattern.dto;

import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;

import java.util.Set;

public record CategoryResponseDto(
        String category,
        Set<String> appPatterns,
        Set<String> domainPatterns
) {
    public static CategoryResponseDto from(CategoryPattern categoryPattern) {
        return new CategoryResponseDto(
                categoryPattern.getCategory(),
                categoryPattern.getAppPatterns(),
                categoryPattern.getDomainPatterns()
        );
    }
}
