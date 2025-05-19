package com.swmStrong.demo.domain.categoryPattern.repository;


import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoryPatternRepository extends MongoRepository<CategoryPattern, String> {
    boolean existsByCategory(String category);
    void deletePatternCategoryByCategory(String category);
}
