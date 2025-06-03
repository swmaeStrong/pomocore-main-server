package com.swmStrong.demo.domain.categoryPattern.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryResponseDto;
import com.swmStrong.demo.domain.categoryPattern.dto.UpdateCategoryRequestDto;
import com.swmStrong.demo.domain.matcher.core.PatternMatcher;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;
import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryPatternServiceImpl implements CategoryPatternService {

    private final CategoryPatternRepository categoryPatternRepository;
    private final PatternMatcher patternMatcher;

    public CategoryPatternServiceImpl(
            CategoryPatternRepository categoryPatternRepository,
            PatternMatcher patternMatcher
    ) {
        this.categoryPatternRepository = categoryPatternRepository;
        this.patternMatcher = patternMatcher;
    }

    @Override
    public void addCategory(CategoryRequestDto categoryRequestDto) {
        if (categoryPatternRepository.existsByCategory(categoryRequestDto.category())) {
            throw new ApiException(ErrorCode.DUPLICATE_CATEGORY);
        }

        //TODO: 색깔에 대한 정규표현식 만들어두기 (#000000 - #FFFFFF)

        CategoryPattern categoryPattern = CategoryPattern.builder()
                .category(categoryRequestDto.category())
                .color(categoryRequestDto.color())
                .priority(categoryRequestDto.priority())
                .build();

        categoryPatternRepository.save(categoryPattern);
    }

    @Override
    public void addPattern(String category, PatternRequestDto patternRequestDto) {
        CategoryPattern categoryPattern = categoryPatternRepository.findByCategory(category)
                        .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryPatternRepository.addPattern(category, patternRequestDto.pattern());
        patternMatcher.insert(patternRequestDto.pattern(), categoryPattern.getId());
    }

    @Override
    public void deletePatternByCategory(String category, PatternRequestDto patternRequestDto) {
        CategoryPattern categoryPattern = categoryPatternRepository.findByCategory(category)
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!categoryPattern.getPatterns().contains(patternRequestDto.pattern())) {
            throw new ApiException(ErrorCode.PATTERN_NOT_FOUND);
        }

        categoryPatternRepository.removePattern(category, patternRequestDto.pattern());
        patternMatcher.init();
    }

    @Override
    public void deleteCategory(String category) {
        if (!categoryPatternRepository.existsByCategory(category)) {
            throw new ApiException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryPatternRepository.deletePatternCategoryByCategory(category);
        patternMatcher.init();
    }

    @Override
    public CategoryPattern getEntityByCategory(String category) {
        return categoryPatternRepository.findByCategory(category)
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Override
    public CategoryResponseDto getCategoryPatternByCategory(String category) {
        return CategoryResponseDto.from(getEntityByCategory(category));
    }

    @Override
    public List<CategoryResponseDto> getCategories() {
        List<CategoryPattern> categoryPatterns = categoryPatternRepository.findAll();
        return categoryPatterns.stream()
                .sorted()
                .map(CategoryResponseDto::from)
                .toList();
    }

    @Override
    public void updateCategory(String category, UpdateCategoryRequestDto updateCategoryRequestDto) {
        //TODO: 중복 카테고리명에 대한 제한 만들기
        CategoryPattern categoryPattern = categoryPatternRepository.findByCategory(category)
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        if (updateCategoryRequestDto.category() != null) {
            categoryPattern.setCategory(updateCategoryRequestDto.category());
            patternMatcher.init();
        }
        if (updateCategoryRequestDto.color() != null) {
            categoryPattern.setColor(updateCategoryRequestDto.color());
        }

        categoryPatternRepository.save(categoryPattern);
    }

    @Override
    public boolean existsByCategory(String category) {
        return categoryPatternRepository.existsByCategory(category);
    }
}
