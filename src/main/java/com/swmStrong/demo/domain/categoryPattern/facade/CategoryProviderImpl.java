package com.swmStrong.demo.domain.categoryPattern.facade;

import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryProviderImpl implements CategoryProvider {

    private final CategoryPatternRepository categoryPatternRepository;

    public CategoryProviderImpl(CategoryPatternRepository categoryPatternRepository) {
        this.categoryPatternRepository = categoryPatternRepository;
    }

    @Override
    public List<String> getCategories() {
        return categoryPatternRepository.findAll().stream()
                .map(CategoryPattern::getCategory)
                .toList();
    }
}
