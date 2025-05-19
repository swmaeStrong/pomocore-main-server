package com.swmStrong.demo.domain.categoryPattern.service;

import com.swmStrong.demo.domain.matcher.core.PatternMatcher;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;
import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CustomCategoryPatternRepository;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoryPatternServiceImpl implements CategoryPatternService {

    private final CategoryPatternRepository categoryPatternRepository;
    private final CustomCategoryPatternRepository customCategoryPatternRepository;
    private final PatternMatcher patternMatcher;

    public CategoryPatternServiceImpl(
            CategoryPatternRepository categoryPatternRepository,
            CustomCategoryPatternRepository customCategoryPatternRepository,
            PatternMatcher patternMatcher
    ) {
        this.categoryPatternRepository = categoryPatternRepository;
        this.customCategoryPatternRepository = customCategoryPatternRepository;
        this.patternMatcher = patternMatcher;
    }

    @Override
    public void addPattern(String category, PatternRequestDto patternRequestDto) {
        if (!categoryPatternRepository.existsByCategory(category)) {
            addCategory(category);
        }
        customCategoryPatternRepository.addPattern(category, patternRequestDto.pattern());
        patternMatcher.insert(patternRequestDto.pattern(), category);
    }

    @Override
    public void deletePatternByCategory(String category, PatternRequestDto patternRequestDto) {
        if (!categoryPatternRepository.existsByCategory(category)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리");
        }
        customCategoryPatternRepository.removePattern(category, patternRequestDto.pattern());
        patternMatcher.init();
    }

    @Override
    public void deleteCategory(String category) {
        if (!categoryPatternRepository.existsByCategory(category)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리");
        }
        categoryPatternRepository.deletePatternCategoryByCategory(category);
        patternMatcher.init();
    }

    private void addCategory(String category) {
        if (categoryPatternRepository.existsByCategory(category)) {
            return;
        }
        CategoryPattern categoryPattern = CategoryPattern.builder()
                .category(category)
                .build();
        categoryPatternRepository.save(categoryPattern);
    }
}
