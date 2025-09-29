package com.swmStrong.demo.domain.pomodoro.entity;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategorizedData 엔티티 테스트")
class CategorizedDataTest {

    @Test
    @DisplayName("Builder로 CategorizedData 생성")
    void shouldCreateCategorizedDataWithBuilder() {
        // given
        String app = "Chrome";
        String url = "https://example.com";
        String title = "Example Site";
        ObjectId categoryId = new ObjectId();

        // when
        CategorizedData categorizedData = CategorizedData.builder()
                .app(app)
                .url(url)
                .title(title)
                .categoryId(categoryId)
                .build();

        // then
        assertThat(categorizedData.getApp()).isEqualTo(app);
        assertThat(categorizedData.getUrl()).isEqualTo(url);
        assertThat(categorizedData.getTitle()).isEqualTo(title);
        assertThat(categorizedData.getCategoryId()).isEqualTo(categoryId);
        assertThat(categorizedData.isIsLLMBased()).isFalse();
    }

    @Test
    @DisplayName("카테고리 ID 업데이트")
    void shouldUpdateCategoryId() {
        // given
        CategorizedData categorizedData = CategorizedData.builder()
                .app("Chrome")
                .url("https://example.com")
                .title("Example Site")
                .categoryId(new ObjectId())
                .build();
        ObjectId newCategoryId = new ObjectId();

        // when
        categorizedData.updateCategoryId(newCategoryId);

        // then
        assertThat(categorizedData.getCategoryId()).isEqualTo(newCategoryId);
    }

    @Test
    @DisplayName("LLM 기반 여부 체크 - true")
    void shouldCheckLLMBasedAsTrue() {
        // given
        CategorizedData categorizedData = CategorizedData.builder()
                .app("Chrome")
                .url("https://example.com")
                .title("Example Site")
                .categoryId(new ObjectId())
                .build();

        // when
        categorizedData.checkLLMBased(true);

        // then
        assertThat(categorizedData.isIsLLMBased()).isTrue();
    }

    @Test
    @DisplayName("LLM 기반 여부 체크 - false")
    void shouldCheckLLMBasedAsFalse() {
        // given
        CategorizedData categorizedData = CategorizedData.builder()
                .app("Chrome")
                .url("https://example.com")
                .title("Example Site")
                .categoryId(new ObjectId())
                .build();
        categorizedData.checkLLMBased(true); // 먼저 true로 설정

        // when
        categorizedData.checkLLMBased(false);

        // then
        assertThat(categorizedData.isIsLLMBased()).isFalse();
    }
}