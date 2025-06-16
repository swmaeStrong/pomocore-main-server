package com.swmStrong.demo.domain.categoryPattern.service;

import com.swmStrong.demo.domain.categoryPattern.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryResponseDto;
import com.swmStrong.demo.domain.categoryPattern.dto.UpdateCategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;
import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.enums.PatternType;

import java.util.List;

public interface CategoryPatternService {
    void addCategory(CategoryRequestDto categoryRequestDto);
    void addPattern(String category, PatternType patternType, PatternRequestDto patternRequestDto);
    void deletePatternByCategory(String category, PatternType patternType, PatternRequestDto patternRequestDto);
    void deleteCategory(String category);
    CategoryPattern getEntityByCategory(String category);
    CategoryResponseDto getCategoryPatternByCategory(String category);
    List<CategoryResponseDto> getCategories();
    void updateCategory(String category, UpdateCategoryRequestDto updateCategoryRequestDto);
    boolean existsByCategory(String category);
}
