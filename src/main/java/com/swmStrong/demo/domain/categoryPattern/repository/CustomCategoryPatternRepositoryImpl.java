package com.swmStrong.demo.domain.categoryPattern.repository;

import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.enums.PatternType;
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
    public void addPattern(String category, PatternType patternType, String newPattern) {
        Query query = new Query(Criteria.where("category").is(category));
        String fieldName = getFieldName(patternType);
        Update update = new Update().addToSet(fieldName, newPattern);
        mongoTemplate.updateFirst(query, update, CategoryPattern.class);
    }

    @Override
    public void removePattern(String category, PatternType patternType, String pattern) {
        Query query = new Query(Criteria.where("category").is(category));
        String fieldName = getFieldName(patternType);
        Update update = new Update().pull(fieldName, pattern);
        mongoTemplate.updateFirst(query, update, CategoryPattern.class);
    }

    @Override
    public Set<String> findPatternsByCategory(String category, PatternType patternType) {
        Query query = new Query(Criteria.where("category").is(category));
        String fieldName = getFieldName(patternType);
        query.fields().include(fieldName);

        CategoryPattern result = mongoTemplate.findOne(query, CategoryPattern.class);

        if (result != null) {
            Set<String> patterns = patternType == PatternType.APP 
                ? result.getAppPatterns() 
                : result.getDomainPatterns();
            return patterns != null ? new HashSet<>(patterns) : new HashSet<>();
        } else {
            return new HashSet<>();
        }
    }

    private String getFieldName(PatternType patternType) {
        return patternType == PatternType.APP ? "appPatterns" : "domainPatterns";
    }
}
