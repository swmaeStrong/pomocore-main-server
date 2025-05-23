package com.swmStrong.demo.domain.categoryPattern.service;

import com.swmStrong.demo.domain.categoryPattern.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryResponseDto;
import com.swmStrong.demo.domain.categoryPattern.dto.UpdateCategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;

import java.util.List;

public interface CategoryPatternService {
    void addCategory(CategoryRequestDto categoryRequestDto);
    void addPattern(String category, PatternRequestDto patternRequestDto);
    void deletePatternByCategory(String category, PatternRequestDto patternRequestDto);
    void deleteCategory(String category);
    CategoryResponseDto getCategoryByCategory(String category);
    List<CategoryResponseDto> getCategories();
    void updateCategory(String category, UpdateCategoryRequestDto updateCategoryRequestDto);
}
