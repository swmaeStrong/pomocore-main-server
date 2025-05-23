package com.swmStrong.demo.domain.categoryPattern.service;

import com.swmStrong.demo.domain.categoryPattern.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryResponseDto;
import com.swmStrong.demo.domain.categoryPattern.dto.ColorRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;

import java.util.List;

public interface CategoryPatternService {
    void addCategory(CategoryRequestDto categoryRequestDto);
    void addPattern(String category, PatternRequestDto patternRequestDto);
    void deletePatternByCategory(String category, PatternRequestDto patternRequestDto);
    void deleteCategory(String category);
    List<CategoryResponseDto> getCategories();
    void setCategoryColor(String category, ColorRequestDto colorRequestDto);
}
