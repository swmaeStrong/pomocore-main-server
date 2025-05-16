package com.swmStrong.demo.domain.patternCategory.repository;


import com.swmStrong.demo.domain.patternCategory.entity.PatternCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PatternCategoryRepository extends MongoRepository<PatternCategory, String> {
    boolean existsByCategory(String category);
    void deletePatternCategoryByCategory(String category);
}
