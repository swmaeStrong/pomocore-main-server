package com.swmStrong.demo.domain.categoryPattern.initializer;

import com.swmStrong.demo.domain.categoryPattern.dto.CategoryPatternJSONDto;
import com.swmStrong.demo.domain.categoryPattern.dto.CategoryRequestDto;
import com.swmStrong.demo.domain.categoryPattern.dto.PatternRequestDto;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import com.swmStrong.demo.domain.categoryPattern.repository.CustomCategoryPatternRepository;
import com.swmStrong.demo.domain.categoryPattern.service.CategoryPatternService;
import com.swmStrong.demo.infra.json.JsonLoader;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class CategoryPatternInitializer implements SmartInitializingSingleton {

    private final CategoryPatternService categoryPatternService;
    private final CategoryPatternRepository categoryPatternRepository;
    private final JsonLoader jsonLoader;

    public CategoryPatternInitializer(
            CategoryPatternService categoryPatternService,
            CategoryPatternRepository categoryPatternRepository,
            JsonLoader jsonLoader
    ) {
        this.categoryPatternService = categoryPatternService;
        this.categoryPatternRepository = categoryPatternRepository;
        this.jsonLoader = jsonLoader;
    }

    @Override
    public void afterSingletonsInstantiated() {
        init();
    }

    private void init() {
        CategoryPatternJSONDto jsonDto = jsonLoader.load("data/category-patterns.json", CategoryPatternJSONDto.class);
        for (CategoryPatternJSONDto.CategoryPatternEntry entry: jsonDto.getCategoryPatterns()) {
            if (!categoryPatternService.existsByCategory(entry.getCategory())) {
                categoryPatternService.addCategory(CategoryRequestDto.of(entry.getCategory(), entry.getColor()));
            }

            Set<String> newPatterns = entry.getPatterns();
            newPatterns.removeAll(categoryPatternRepository.findPatternsByCategory(entry.getCategory()));
            for (String pattern: newPatterns) {
                categoryPatternService.addPattern(entry.getCategory(), PatternRequestDto.of(pattern));
            }
        }
    }
}