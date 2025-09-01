package com.swmStrong.demo.domain.categoryPattern.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryResponseDto;
import com.swmStrong.demo.domain.categoryPattern.dto.UpdateCategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;
import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.enums.PatternType;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryPatternServiceImpl implements CategoryPatternService {

    private final CategoryPatternRepository categoryPatternRepository;

    public CategoryPatternServiceImpl(
            CategoryPatternRepository categoryPatternRepository
    ) {
        this.categoryPatternRepository = categoryPatternRepository;
    }

    @Override
    public void addCategory(CategoryRequestDto categoryRequestDto) {
        if (categoryPatternRepository.existsByCategory(categoryRequestDto.category())) {
            throw new ApiException(ErrorCode.DUPLICATE_CATEGORY);
        }

        CategoryPattern categoryPattern = CategoryPattern.builder()
                .category(categoryRequestDto.category())
                .priority(categoryRequestDto.priority())
                .build();

        categoryPatternRepository.save(categoryPattern);
    }

    @Override
    public void addPattern(String category, PatternType patternType, PatternRequestDto patternRequestDto) {
        CategoryPattern categoryPattern = categoryPatternRepository.findByCategory(category)
                        .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryPatternRepository.addPattern(category, patternType, patternRequestDto.pattern());
    }

    @Override
    public void deletePatternByCategory(String category, PatternType patternType, PatternRequestDto patternRequestDto) {
        CategoryPattern categoryPattern = categoryPatternRepository.findByCategory(category)
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        java.util.Set<String> patterns = patternType == PatternType.APP 
            ? categoryPattern.getAppPatterns() 
            : categoryPattern.getDomainPatterns();
            
        if (patterns == null || !patterns.contains(patternRequestDto.pattern())) {
            throw new ApiException(ErrorCode.PATTERN_NOT_FOUND);
        }

        categoryPatternRepository.removePattern(category, patternType, patternRequestDto.pattern());
    }

    @Override
    public void deleteCategory(String category) {
        if (!categoryPatternRepository.existsByCategory(category)) {
            throw new ApiException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        categoryPatternRepository.deletePatternCategoryByCategory(category);
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
        CategoryPattern categoryPattern = categoryPatternRepository.findByCategory(category)
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND));

        if (updateCategoryRequestDto.category() != null && 
            !updateCategoryRequestDto.category().equals(category) &&
            categoryPatternRepository.existsByCategory(updateCategoryRequestDto.category())) {
            throw new ApiException(ErrorCode.DUPLICATE_CATEGORY);
        }

        if (updateCategoryRequestDto.category() != null) {
            categoryPattern.updateCategory(updateCategoryRequestDto.category());
        }
        categoryPatternRepository.save(categoryPattern);
    }
}
