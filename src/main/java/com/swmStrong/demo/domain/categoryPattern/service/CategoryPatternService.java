package com.swmStrong.demo.domain.categoryPattern.service;

import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;

public interface CategoryPatternService {
    void addPattern(String category, PatternRequestDto patternRequestDto);
    void deletePatternByCategory(String category, PatternRequestDto patternRequestDto);
    void deleteCategory(String category);
}
