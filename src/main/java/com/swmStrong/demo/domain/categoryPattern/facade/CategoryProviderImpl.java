package com.swmStrong.demo.domain.categoryPattern.facade;

import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import org.bson.types.ObjectId;
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
                .sorted()
                .map(CategoryPattern::getCategory)
                .toList();
    }

    @Override
    public String getCategoryById(ObjectId categoryId) {
        return categoryPatternRepository.findById(categoryId)
                .orElseThrow(IllegalArgumentException::new)
                .getCategory();
    }

    @Override
    public ObjectId getCategoryIdByCategory(String category) {
        return categoryPatternRepository.findByCategory(category)
                .orElseThrow(IllegalArgumentException::new)
                .getId();
    }
}
