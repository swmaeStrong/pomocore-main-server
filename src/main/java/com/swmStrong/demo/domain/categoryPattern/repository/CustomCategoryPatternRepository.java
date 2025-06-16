package com.swmStrong.demo.domain.categoryPattern.repository;

import com.swmStrong.demo.domain.categoryPattern.enums.PatternType;
import java.util.Set;

public interface CustomCategoryPatternRepository {
    void addPattern(String category, PatternType patternType, String newPattern);
    void removePattern(String category, PatternType patternType, String pattern);
    Set<String> findPatternsByCategory(String category, PatternType patternType);
}
