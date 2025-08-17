package com.swmStrong.demo.domain.leaderboard.entity;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "leaderboard",
        indexes = {
                @Index(name="idx_category_period", columnList="categoryId, periodType, periodKey")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Leaderboard {
    @Id
    private String id;

    private String userId;

    private String categoryId;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private String periodKey;

    private Integer ranking;

    private double score;

    private LocalDateTime createdAt;

    @Builder
    public Leaderboard(String userId, String categoryId, PeriodType periodType, String periodKey, Integer ranking, double score) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.categoryId = categoryId;
        this.periodType = periodType;
        this.periodKey = periodKey;
        this.ranking = ranking;
        this.score = score;
        this.createdAt = LocalDateTime.now();
    }
}
