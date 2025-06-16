package com.swmStrong.demo.domain.categoryPattern.initializer;

import com.swmStrong.demo.domain.categoryPattern.dto.CategoryPatternJSONDto;
import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import com.swmStrong.demo.domain.categoryPattern.service.CategoryPatternService;
import com.swmStrong.demo.infra.json.JsonLoader;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CategoryPatternInitializer implements SmartInitializingSingleton {

    private final CategoryPatternRepository categoryPatternRepository;
    private final JsonLoader jsonLoader;

    public CategoryPatternInitializer(
            CategoryPatternRepository categoryPatternRepository,
            JsonLoader jsonLoader
    ) {
        this.categoryPatternRepository = categoryPatternRepository;
        this.jsonLoader = jsonLoader;
    }

    @Override
    public void afterSingletonsInstantiated() {
        init();
    }

    //TODO: 트랜잭션으로 분리
    private void init() {
        CategoryPatternJSONDto jsonDto = jsonLoader.load("data/category-patterns.json", CategoryPatternJSONDto.class);

        List<CategoryPattern> existingPatterns = categoryPatternRepository.findAll();
        Map<String, CategoryPattern> existingPatternsMap = existingPatterns.stream()
                .collect(Collectors.toMap(
                    CategoryPattern::getCategory, 
                    p -> p,
                    (existing, duplicate) -> existing // Keep first occurrence if duplicates exist
                ));
        
        List<CategoryPattern> patternsToSave = new ArrayList<>();
        
        for (CategoryPatternJSONDto.CategoryPatternEntry entry: jsonDto.getCategoryPatterns()) {
            CategoryPattern categoryPattern = existingPatternsMap.get(entry.getCategory());
            
            if (categoryPattern == null) {
                categoryPattern = CategoryPattern.builder()
                        .category(entry.getCategory())
                        .priority(entry.getPriority())
                        .appPatterns(new HashSet<>(entry.getAppPatterns()))
                        .domainPatterns(new HashSet<>(entry.getDomainPatterns()))
                        .build();
                patternsToSave.add(categoryPattern);
            } else {
                boolean needsUpdate = false;

                if (categoryPattern.getPriority() == null || !categoryPattern.getPriority().equals(entry.getPriority())) {
                    categoryPattern.updatePriority(entry.getPriority());
                    needsUpdate = true;
                }

                Set<String> currentAppPatterns = categoryPattern.getAppPatterns() != null 
                    ? categoryPattern.getAppPatterns() 
                    : Collections.emptySet();
                if (!currentAppPatterns.containsAll(entry.getAppPatterns())) {
                    needsUpdate = true;
                }

                Set<String> currentDomainPatterns = categoryPattern.getDomainPatterns() != null 
                    ? categoryPattern.getDomainPatterns() 
                    : Collections.emptySet();
                if (!currentDomainPatterns.containsAll(entry.getDomainPatterns())) {
                    needsUpdate = true;
                }
                
                if (needsUpdate) {
                    Set<String> finalAppPatterns = new HashSet<>();
                    if (categoryPattern.getAppPatterns() != null) {
                        finalAppPatterns.addAll(categoryPattern.getAppPatterns());
                    }
                    finalAppPatterns.addAll(entry.getAppPatterns());
                    
                    Set<String> finalDomainPatterns = new HashSet<>();
                    if (categoryPattern.getDomainPatterns() != null) {
                        finalDomainPatterns.addAll(categoryPattern.getDomainPatterns());
                    }
                    finalDomainPatterns.addAll(entry.getDomainPatterns());
                    
                    CategoryPattern updatedPattern = CategoryPattern.builder()
                            .id(categoryPattern.getId())
                            .category(categoryPattern.getCategory())
                            .priority(categoryPattern.getPriority())
                            .appPatterns(finalAppPatterns)
                            .domainPatterns(finalDomainPatterns)
                            .build();
                    
                    patternsToSave.add(updatedPattern);
                }
            }
        }

        if (!patternsToSave.isEmpty()) {
            categoryPatternRepository.saveAll(patternsToSave);
        }
    }
}