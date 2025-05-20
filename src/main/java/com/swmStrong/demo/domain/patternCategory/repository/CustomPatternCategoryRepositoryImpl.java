package com.swmStrong.demo.domain.patternCategory.repository;

import com.swmStrong.demo.domain.patternCategory.entity.PatternCategory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class CustomPatternCategoryRepositoryImpl implements CustomPatternCategoryRepository {
    private final MongoTemplate mongoTemplate;

    public CustomPatternCategoryRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void addPattern(String category, String newPattern) {
        Query query = new Query(Criteria.where("category").is(category));
        Update update = new Update().addToSet("patterns", newPattern);
        mongoTemplate.updateFirst(query, update, PatternCategory.class);
    }

    @Override
    public void removePattern(String category, String pattern) {
        Query query = new Query(Criteria.where("category").is(category));
        Update update = new Update().pull("patterns", pattern);
        mongoTemplate.updateFirst(query, update, PatternCategory.class);
    }
}
