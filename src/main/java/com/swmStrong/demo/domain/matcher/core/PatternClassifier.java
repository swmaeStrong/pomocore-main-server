package com.swmStrong.demo.domain.matcher.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import com.swmStrong.demo.domain.common.util.DomainExtractor;
import com.swmStrong.demo.domain.common.util.Trie;
import com.swmStrong.demo.infra.LLM.LLMClassifier;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class PatternClassifier {
    private final CategoryPatternRepository categoryPatternRepository;
    private final Cache<String, ObjectId> classificationCache;
    private final LLMClassifier classifier;

    public Trie<ObjectId> appTrie;
    public Trie<ObjectId> domainTrie;

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
        appTrie = new Trie<>();
        domainTrie = new Trie<>();

        List<CategoryPattern> categoryPatterns = categoryPatternRepository.findAll();
        for (CategoryPattern categoryPattern: categoryPatterns) {
            ObjectId categoryId = categoryPattern.getId();

            if (categoryPattern.getAppPatterns() != null && !categoryPattern.getAppPatterns().isEmpty()) {
                for (String pattern: categoryPattern.getAppPatterns()) {
                    appTrie.insert(categoryId, pattern);
                }
            }

            if (categoryPattern.getDomainPatterns() != null && !categoryPattern.getDomainPatterns().isEmpty()) {
                for (String pattern: categoryPattern.getDomainPatterns()) {
                    domainTrie.insert(categoryId, pattern);
                }
            }
        }
        log.info("app and domain tries initialized");
    }

    public ObjectId classify(String app, String title, String url) {
        log.trace("start classify: {}, {}, {}", app, title, url);
        String query = getQuery(app, title, url);
        ObjectId objectId;

        // trie (1차 분류) -> 1차 분류에서 걸러지지 않는 것: 브라우징류의 모호한 것들
        objectId = classifyFromAppTrie(app);
        if (objectId != null) return objectId;
        //trie (1.5차 분류) -> 1차 분류에서 걸러지지 않지만, 브라우징에서 확연하게 걸러낼 수 있는 것들 (domain 기반)
        if (url != null && !url.isEmpty()) {
            objectId = classifyFromDomainTrie(url);
            if (objectId != null) return objectId;
        }
        // cache (2차 분류) -> 하위 레이어에서 분류한 것들을 캐싱
        objectId = classifyFromCache(query);
        if (objectId != null) return objectId;
        // ML (3차 분류) -> 하위 레이어에서 분류한 것들을 통해 유사도 기반 클러스터링
        objectId = classifyFromML(app, title, url);
        if (objectId != null) return putCache(query, objectId);
        // LLM (4차 분류) -> 여기까지 도달한 경우 프롬프팅을 통해 카테고리 도출
        objectId = classifyFromLLM(query);
        if (objectId != null) return putCache(query, objectId);
        return categoryPatternRepository.findByCategory("Uncategorized")
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND)).getId();
    }

    private ObjectId classifyFromAppTrie(String app) {
        log.trace("trie layer: {}", app);
        return appTrie.search(app, false);
    }

    private ObjectId classifyFromDomainTrie(String domain) {
        // Extract clean domain from URL if it's a full URL
        String cleanDomain = DomainExtractor.extractDomain(domain);
        if (cleanDomain == null) {
            log.warn("Failed to extract domain from: {}", domain);
            return null;
        }
        log.trace("trie layer with domain: {} (extracted: {})", domain, cleanDomain);
        return domainTrie.search(cleanDomain, true);
    }

    private ObjectId classifyFromCache(String query) {
        log.trace("cache layer: {}", query);
        return classificationCache.getIfPresent(query);
    }

    private ObjectId classifyFromML(String app, String title, String url) {
        // ML 관련 로직 제대로 생각해서 만들어내기
        log.trace("ML layer: {}, {}, {}", app, title, url);
        return null;
    }

    private ObjectId classifyFromLLM(String query) {
        log.trace("LLM layer: {}", query);
        String category = classifier.classify(query);
        Optional<CategoryPattern> categoryId = categoryPatternRepository.findByCategory(category);
        return categoryId.map(CategoryPattern::getId).orElse(null);
    }

    private String getQuery(String app, String title, String url) {
        return String.format("app: %s, title: %s, url: %s", app, title, url);
    }

    private ObjectId putCache(String query, ObjectId objectId) {
        classificationCache.put(query, objectId);
        return objectId;
    }
}
