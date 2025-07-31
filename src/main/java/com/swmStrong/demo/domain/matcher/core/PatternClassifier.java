package com.swmStrong.demo.domain.matcher.core;

import com.github.benmanes.caffeine.cache.Cache;
import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.entity.CategoryPattern;
import com.swmStrong.demo.domain.categoryPattern.repository.CategoryPatternRepository;
import com.swmStrong.demo.domain.common.util.DomainExtractor;
import com.swmStrong.demo.domain.common.util.Trie;
import com.swmStrong.demo.infra.LLM.LLMClassifier;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class PatternClassifier implements InitializingBean {
    private final CategoryPatternRepository categoryPatternRepository;
    private final Cache<String, ObjectId> classificationCache;
    private final LLMClassifier classifier;

    public Trie<ObjectId> appTrie;
    public Trie<ObjectId> domainTrie;
    
    // 초기화 완료 상태 추적
    private volatile boolean initialized = false;

    public PatternClassifier(
            CategoryPatternRepository categoryPatternRepository,
            Cache<String, ObjectId> classificationCache,
            LLMClassifier classifier
    ) {
        this.categoryPatternRepository = categoryPatternRepository;
        this.classificationCache = classificationCache;
        this.classifier = classifier;
        log.info("PatternClassifier constructor completed, initialization will follow");
    }

    @Override
    public void afterPropertiesSet() {
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
        initialized = true; // 초기화 완료 플래그 설정
        log.info("PatternClassifier initialized successfully - appTrie and domainTrie are ready");
    }

    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        if (!initialized) {
            log.error("CRITICAL: PatternClassifier not initialized after application startup!");
            afterPropertiesSet();
        } else {
            log.info("PatternClassifier ready for service - Trie structures confirmed");
        }
    }

    public ClassifiedResult classify(String app, String title, String url) {
        log.trace("start classify: {}, {}, {}", app, title, url);

        if (!initialized || appTrie == null || domainTrie == null) {
            log.warn("PatternClassifier not ready, ensuring initialization");
            ensureInitialized();
        }
        
        String query = getQuery(app, title, url);
        ObjectId objectId;

        // trie (1차 분류) -> 1차 분류에서 걸러지지 않는 것: 브라우징류의 모호한 것들
        objectId = classifyFromAppTrie(app);
        if (objectId != null) return new ClassifiedResult(objectId, false);
        //trie (1.5차 분류) -> 1차 분류에서 걸러지지 않지만, 브라우징에서 확연하게 걸러낼 수 있는 것들 (domain 기반)
        if (url != null && !url.isEmpty()) {
            objectId = classifyFromDomainTrie(url);
            if (objectId != null) return new ClassifiedResult(objectId, false);
        }
        // cache (2차 분류) -> 하위 레이어에서 분류한 것들을 캐싱
        objectId = classifyFromCache(query);
        if (objectId != null) return new ClassifiedResult(objectId, true);
        // ML (3차 분류) -> 하위 레이어에서 분류한 것들을 통해 유사도 기반 클러스터링
        objectId = classifyFromML(app, title, url);
        if (objectId != null) return putCache(query, objectId);
        // LLM (4차 분류) -> 여기까지 도달한 경우 프롬프팅을 통해 카테고리 도출
        objectId = classifyFromLLM(query);
        if (objectId != null) return putCache(query, objectId);
        return new ClassifiedResult(categoryPatternRepository.findByCategory("Uncategorized")
                .orElseThrow(() -> new ApiException(ErrorCode.CATEGORY_NOT_FOUND)).getId(), true);
    }

    private ObjectId classifyFromAppTrie(String app) {
        log.trace("trie layer: {}", app);
        ensureInitialized(); // 안전 장치: 지연 초기화 확인
        return appTrie != null ? appTrie.search(app, false) : null;
    }

    private ObjectId classifyFromDomainTrie(String domain) {
        // Extract clean domain from URL if it's a full URL
        String cleanDomain = DomainExtractor.extractDomain(domain);
        if (cleanDomain == null) {
            log.warn("Failed to extract domain from: {}", domain);
            return null;
        }
        log.trace("trie layer with domain: {} (extracted: {})", domain, cleanDomain);
        ensureInitialized(); // 안전 장치: 지연 초기화 확인
        return domainTrie != null ? domainTrie.search(cleanDomain, true) : null;
    }
    
    /**
     * 초기화가 완료되지 않은 경우 동기적으로 초기화를 수행합니다.
     * Race condition을 방지하기 위해 synchronized 블록을 사용합니다.
     */
    private synchronized void ensureInitialized() {
        if (!initialized) {
            log.warn("PatternClassifier not initialized, performing lazy initialization");
            afterPropertiesSet();
        }
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

    private ClassifiedResult putCache(String query, ObjectId objectId) {
        classificationCache.put(query, objectId);
        return new ClassifiedResult(objectId, true);
    }

    public record ClassifiedResult(
            ObjectId categoryPatternId,
            boolean isLLMBased
    ) {

    }
}
