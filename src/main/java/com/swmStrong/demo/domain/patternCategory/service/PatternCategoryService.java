package com.swmStrong.demo.domain.patternCategory.service;

import com.swmStrong.demo.domain.patternCategory.dto.PatternRequestDto;

public interface PatternCategoryService {
    void addPattern(PatternRequestDto patternRequestDto);
    void deletePatternByCategory(PatternRequestDto patternRequestDto);
    void deleteCategory(String category);
}
