package com.swmStrong.demo.domain.categoryPattern.service;

import com.swmStrong.demo.domain.categoryPattern.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryResponseDto;
import com.swmStrong.demo.domain.categoryPattern.dto.UpdateCategoryRequestDto;
import com.swmStrong.demo.domain.matcher.core.PatternMatcher;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;
import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CustomCategoryPatternRepository;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public void addCategory(CategoryRequestDto categoryRequestDto) {
        if (categoryPatternRepository.existsByCategory(categoryRequestDto.category())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리");
        }

        //TODO: 색깔에 대한 정규표현식 만들어두기 (#000000 - #FFFFFF)

        CategoryPattern categoryPattern = CategoryPattern.builder()
                .category(categoryRequestDto.category())
                .color(categoryRequestDto.color())
                .build();

        categoryPatternRepository.save(categoryPattern);
    }

    @Override
    public void addPattern(String category, PatternRequestDto patternRequestDto) {
        if (!categoryPatternRepository.existsByCategory(category)) {
            throw new IllegalArgumentException("존재하지 않는 카테고리");
        }

        customCategoryPatternRepository.addPattern(category, patternRequestDto.pattern());
        patternMatcher.insert(patternRequestDto.pattern(), category);
    }

    @Override
    public void deletePatternByCategory(String category, PatternRequestDto patternRequestDto) {
        CategoryPattern categoryPattern = categoryPatternRepository.findByCategory(category)
                .orElseThrow(IllegalArgumentException::new);

        if (!categoryPattern.getPatterns().contains(patternRequestDto.pattern())) {
            throw new IllegalArgumentException("존재하지 않는 패턴");
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

    @Override
    public CategoryResponseDto getCategoryByCategory(String category) {
        CategoryPattern categoryPattern = categoryPatternRepository.findByCategory(category)
                .orElseThrow(IllegalArgumentException::new);
        return CategoryResponseDto.from(categoryPattern);
    }

    @Override
    public List<CategoryResponseDto> getCategories() {
        List<CategoryPattern> categoryPatterns = categoryPatternRepository.findAll();
        return categoryPatterns.stream()
                .map(CategoryResponseDto::from)
                .toList();
    }

    @Override
    public void updateCategory(String category, UpdateCategoryRequestDto updateCategoryRequestDto) {
        CategoryPattern categoryPattern = categoryPatternRepository.findByCategory(category)
                .orElseThrow(IllegalArgumentException::new);

        if (updateCategoryRequestDto.category() != null) {
            categoryPattern.setCategory(updateCategoryRequestDto.category());
            patternMatcher.init();
        }
        if (updateCategoryRequestDto.color() != null) {
            categoryPattern.setColor(updateCategoryRequestDto.color());
        }

        categoryPatternRepository.save(categoryPattern);
    }

}
