package com.swmStrong.demo.domain.categoryPattern.facade;

import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryProviderImpl implements CategoryProvider {

    private final CategoryPatternRepository categoryPatternRepository;

    public CategoryProviderImpl(CategoryPatternRepository categoryPatternRepository) {
        this.categoryPatternRepository = categoryPatternRepository;
    }

    @Override
    public List<String> getCategories() {
        List<String> categoryList = new ArrayList<>(categoryPatternRepository.findAll().stream().map(CategoryPattern::getCategory).toList());
        categoryList.add("work");
        categoryList.add("sessionScore");
        categoryList.add("sessionCount");
        return categoryList;
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

    @Override
    public Map<ObjectId, String> getCategoryMapById() {
        return categoryPatternRepository.findAll().stream()
                .collect(Collectors.toMap(CategoryPattern::getId, CategoryPattern::getCategory));
    }

    @Override
    public Map<String, ObjectId> getCategoryMapByCategory() {
        return categoryPatternRepository.findAll().stream()
                .collect(Collectors.toMap(CategoryPattern::getCategory, CategoryPattern::getId));
    }
}
