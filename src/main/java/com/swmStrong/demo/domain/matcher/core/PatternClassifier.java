package com.swmStrong.demo.domain.matcher.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import com.swmStrong.demo.infra.LLM.LLMClassifier;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class PatternClassifier {
    private final CategoryPatternRepository categoryPatternRepository;
    private final Cache<String, ObjectId> classificationCache;
    private final LLMClassifier classifier;

    public Trie trie;

    public PatternClassifier(
            CategoryPatternRepository categoryPatternRepository,
            Cache<String, ObjectId> classificationCache,
            LLMClassifier classifier
    ) {
        this.categoryPatternRepository = categoryPatternRepository;
        this.classificationCache = classificationCache;
        this.classifier = classifier;
    }

    @PostConstruct
    public void init() {
        trie = new Trie();
        for (CategoryPattern categoryPattern: categoryPatternRepository.findAll()) {
            if (categoryPattern.getPatterns() == null || categoryPattern.getPatterns().isEmpty()) {
                continue;
            }
            for (String pattern: categoryPattern.getPatterns()) {
                ObjectId categoryId = categoryPattern.getId();
                trie.insert(categoryId, pattern);
            }
        }
        log.info("trie initialized");
    }

    public ObjectId classify(String app, String title, String domain) {
        log.info("start classify: {}, {}, {}", app, title, domain);
        String query = getQuery(app, title, domain);
        ObjectId objectId;

        // trie (1차 분류) -> 1차 분류에서 걸러지지 않는 것: 브라우징류의 모호한 것들
        objectId = classifyFromTrie(app);
        //TODO: trie (1.5차 분류) -> 1차 분류에서 걸러지지 않지만, 브라우징에서 확연하게 걸러낼 수 있는 것들 (domain 기반)
        if (objectId != null) return objectId;
        // cache (2차 분류) -> 하위 레이어에서 분류한 것들을 캐싱
        objectId = classifyFromCache(query);
        if (objectId != null) return objectId;
        //TODO: ML (3차 분류) -> 하위 레이어에서 분류한 것들을 통해 유사도 기반 클러스터링
        objectId = classifyFromML(app, title, domain);
        if (objectId != null) return putCache(query, objectId);
        // LLM (4차 분류) -> 여기까지 도달한 경우 프롬프팅을 통해 카테고리 도출
        objectId = classifyFromLLM(query);
        if (objectId != null) return putCache(query, objectId);
        return objectId;
    }

    private ObjectId classifyFromTrie(String app) {
        log.info("trie layer: {}", app);
        return trie.search(app);
    }

    private ObjectId classifyFromCache(String query) {
        log.info("cache layer: {}", query);
        return classificationCache.getIfPresent(query);
    }

    private ObjectId classifyFromML(String app, String title, String domain) {
        //TODO: ML 관련 로직 제대로 생각해서 만들어내기
        log.info("ML layer: {}, {}, {}", app, title, domain);
        return null;
    }

    private ObjectId classifyFromLLM(String query) {
        //TODO: 현재 모델은 과부하가 발생하는 경우가 있음(503에러). 재시도 로직을 만들어볼 것
        log.info("LLM layer: {}", query);
        String category = classifier.classify(query);
        Optional<CategoryPattern> categoryId = categoryPatternRepository.findByCategory(category);
        return categoryId.map(CategoryPattern::getId).orElse(null);
    }

    private String getQuery(String app, String title, String domain) {
        return String.format("app: %s, title: %s, domain: %s", app, title, domain);
    }

    private ObjectId putCache(String query, ObjectId objectId) {
        classificationCache.put(query, objectId);
        return objectId;
    }
}
