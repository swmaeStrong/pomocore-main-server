package com.swmStrong.demo.domain.categoryPattern.repository;

import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
public class CustomCategoryPatternRepositoryImpl implements CustomCategoryPatternRepository {
    private final MongoTemplate mongoTemplate;

    public CustomCategoryPatternRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void addPattern(String category, String newPattern) {
        Query query = new Query(Criteria.where("category").is(category));
        Update update = new Update().addToSet("patterns", newPattern);
        mongoTemplate.updateFirst(query, update, CategoryPattern.class);
    }

    @Override
    public void removePattern(String category, String pattern) {
        Query query = new Query(Criteria.where("category").is(category));
        Update update = new Update().pull("patterns", pattern);
        mongoTemplate.updateFirst(query, update, CategoryPattern.class);
    }

    @Override
    public Set<String> findPatternsByCategory(String category) {
        Query query = new Query(Criteria.where("category").is(category));
        query.fields().include("patterns");

        CategoryPattern result = mongoTemplate.findOne(query, CategoryPattern.class);

        if (result != null && result.getPatterns() != null) {
            return new HashSet<>(result.getPatterns());
        } else {
            return new HashSet<>();
        }
    }
}
