package com.swmStrong.demo.domain.categoryPattern.entity;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryPattern 엔티티 테스트")
class CategoryPatternTest {

    @Test
    @DisplayName("Builder로 CategoryPattern 생성")
    void shouldCreateCategoryPatternWithBuilder() {
        // given
        ObjectId id = new ObjectId();
        String category = "개발";
        Integer priority = 1;
        Set<String> appPatterns = new HashSet<>(Arrays.asList("IntelliJ IDEA", "Visual Studio Code"));
        Set<String> domainPatterns = new HashSet<>(Arrays.asList("github.com", "stackoverflow.com"));

        // when
        CategoryPattern categoryPattern = CategoryPattern.builder()
                .id(id)
                .category(category)
                .priority(priority)
                .appPatterns(appPatterns)
                .domainPatterns(domainPatterns)
                .build();

        // then
        assertThat(categoryPattern.getId()).isEqualTo(id);
        assertThat(categoryPattern.getCategory()).isEqualTo(category);
        assertThat(categoryPattern.getPriority()).isEqualTo(priority);
        assertThat(categoryPattern.getAppPatterns()).isEqualTo(appPatterns);
        assertThat(categoryPattern.getDomainPatterns()).isEqualTo(domainPatterns);
    }

    @Test
    @DisplayName("카테고리 이름 업데이트")
    void shouldUpdateCategory() {
        // given
        CategoryPattern categoryPattern = CategoryPattern.builder()
                .id(new ObjectId())
                .category("기존카테고리")
                .priority(1)
                .appPatterns(new HashSet<>())
                .domainPatterns(new HashSet<>())
                .build();
        String newCategory = "새카테고리";

        // when
        categoryPattern.updateCategory(newCategory);

        // then
        assertThat(categoryPattern.getCategory()).isEqualTo(newCategory);
    }

    @Test
    @DisplayName("우선순위 업데이트")
    void shouldUpdatePriority() {
        // given
        CategoryPattern categoryPattern = CategoryPattern.builder()
                .id(new ObjectId())
                .category("개발")
                .priority(1)
                .appPatterns(new HashSet<>())
                .domainPatterns(new HashSet<>())
                .build();
        int newPriority = 5;

        // when
        categoryPattern.updatePriority(newPriority);

        // then
        assertThat(categoryPattern.getPriority()).isEqualTo(newPriority);
    }

    @Test
    @DisplayName("우선순위로 CategoryPattern 정렬 - 일반적인 경우")
    void shouldCompareCategoryPatternsByPriority() {
        // given
        CategoryPattern pattern1 = CategoryPattern.builder()
                .id(new ObjectId())
                .category("개발")
                .priority(3)
                .appPatterns(new HashSet<>())
                .domainPatterns(new HashSet<>())
                .build();

        CategoryPattern pattern2 = CategoryPattern.builder()
                .id(new ObjectId())
                .category("디자인")
                .priority(1)
                .appPatterns(new HashSet<>())
                .domainPatterns(new HashSet<>())
                .build();

        CategoryPattern pattern3 = CategoryPattern.builder()
                .id(new ObjectId())
                .category("업무")
                .priority(2)
                .appPatterns(new HashSet<>())
                .domainPatterns(new HashSet<>())
                .build();

        // when
        List<CategoryPattern> sortedPatterns = Arrays.asList(pattern1, pattern2, pattern3);
        sortedPatterns.sort(CategoryPattern::compareTo);

        // then
        assertThat(sortedPatterns.get(0)).isEqualTo(pattern2); // priority 1
        assertThat(sortedPatterns.get(1)).isEqualTo(pattern3); // priority 2
        assertThat(sortedPatterns.get(2)).isEqualTo(pattern1); // priority 3
    }

    @Test
    @DisplayName("우선순위로 CategoryPattern 정렬 - null 우선순위 처리")
    void shouldCompareCategoryPatternsWithNullPriority() {
        // given
        CategoryPattern patternWithPriority = CategoryPattern.builder()
                .id(new ObjectId())
                .category("개발")
                .priority(1)
                .appPatterns(new HashSet<>())
                .domainPatterns(new HashSet<>())
                .build();

        CategoryPattern patternWithNullPriority = CategoryPattern.builder()
                .id(new ObjectId())
                .category("기타")
                .priority(null)
                .appPatterns(new HashSet<>())
                .domainPatterns(new HashSet<>())
                .build();

        // when
        int comparison = patternWithPriority.compareTo(patternWithNullPriority);

        // then
        assertThat(comparison).isLessThan(0); // null이 마지막에 오므로 일반 priority가 앞에 옴
    }

    @Test
    @DisplayName("같은 우선순위의 CategoryPattern 비교")
    void shouldCompareCategoryPatternsWithSamePriority() {
        // given
        CategoryPattern pattern1 = CategoryPattern.builder()
                .id(new ObjectId())
                .category("개발")
                .priority(1)
                .appPatterns(new HashSet<>())
                .domainPatterns(new HashSet<>())
                .build();

        CategoryPattern pattern2 = CategoryPattern.builder()
                .id(new ObjectId())
                .category("디자인")
                .priority(1)
                .appPatterns(new HashSet<>())
                .domainPatterns(new HashSet<>())
                .build();

        // when
        int comparison = pattern1.compareTo(pattern2);

        // then
        assertThat(comparison).isEqualTo(0); // 같은 우선순위
    }

    @Test
    @DisplayName("복합 업데이트 시나리오")
    void shouldHandleComplexUpdateScenario() {
        // given
        CategoryPattern categoryPattern = CategoryPattern.builder()
                .id(new ObjectId())
                .category("기존카테고리")
                .priority(1)
                .appPatterns(new HashSet<>(Arrays.asList("App1", "App2")))
                .domainPatterns(new HashSet<>(Arrays.asList("domain1.com", "domain2.com")))
                .build();

        // when
        categoryPattern.updateCategory("업데이트된카테고리");
        categoryPattern.updatePriority(5);

        // then
        assertThat(categoryPattern.getCategory()).isEqualTo("업데이트된카테고리");
        assertThat(categoryPattern.getPriority()).isEqualTo(5);
        assertThat(categoryPattern.getAppPatterns()).containsExactly("App1", "App2");
        assertThat(categoryPattern.getDomainPatterns()).containsExactly("domain1.com", "domain2.com");
    }
}