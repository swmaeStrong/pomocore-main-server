package com.swmStrong.demo.domain.patternCategory.service;

import com.swmStrong.demo.domain.matcher.core.PatternMatcher;
import com.swmStrong.demo.domain.patternCategory.dto.PatternRequestDto;
import com.swmStrong.demo.domain.patternCategory.entity.PatternCategory;
import com.swmStrong.demo.domain.patternCategory.repository.CustomPatternCategoryRepository;
import com.swmStrong.demo.domain.patternCategory.repository.PatternCategoryRepository;
import org.springframework.stereotype.Service;

@Service
public class PatternCategoryServiceImpl implements PatternCategoryService {

    private final PatternCategoryRepository patternCategoryRepository;
    private final CustomPatternCategoryRepository customPatternCategoryRepository;
    private final PatternMatcher patternMatcher;

    public PatternCategoryServiceImpl(
            PatternCategoryRepository patternCategoryRepository,
            CustomPatternCategoryRepository customPatternCategoryRepository,
            PatternMatcher patternMatcher
    ) {
        this.patternCategoryRepository = patternCategoryRepository;
        this.customPatternCategoryRepository = customPatternCategoryRepository;
        this.patternMatcher = patternMatcher;
    }

    @Override
    public void addPattern(PatternRequestDto patternRequestDto) {
        if (!patternCategoryRepository.existsByCategory(patternRequestDto.category())) {
            addCategory(patternRequestDto.category());
        }
        customPatternCategoryRepository.addPattern(patternRequestDto.category(), patternRequestDto.pattern());
        patternMatcher.insert(patternRequestDto.pattern(), patternRequestDto.category());
    }

    @Override
    public void deletePatternByCategory(PatternRequestDto patternRequestDto) {
        if (!patternCategoryRepository.existsByCategory(patternRequestDto.category())) {
            throw new IllegalArgumentException("존재하지 않는 카테고리");
        }
        customPatternCategoryRepository.removePattern(patternRequestDto.category(), patternRequestDto.pattern());
        patternMatcher.init();
    }

    @Override
    public void deleteCategory(String category) {
        if (!patternCategoryRepository.existsByCategory(category)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리");
        }
        patternCategoryRepository.deletePatternCategoryByCategory(category);
        patternMatcher.init();
    }

    private void addCategory(String category) {
        if (patternCategoryRepository.existsByCategory(category)) {
            return;
        }
        PatternCategory patternCategory = PatternCategory.builder()
                .category(category)
                .build();
        patternCategoryRepository.save(patternCategory);
    }
}
