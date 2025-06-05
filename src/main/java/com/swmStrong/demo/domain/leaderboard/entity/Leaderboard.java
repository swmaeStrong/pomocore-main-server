package com.swmStrong.demo.domain.leaderboard.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
        name = "leaderboard",
        indexes = {
                @Index(name="idx_category_period", columnList="categoryId, periodType, periodKey")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Leaderboard extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId;

    private String categoryId;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private String periodKey;

    private Integer rank;

    private double score;

    @Builder
    public Leaderboard(String userId, String categoryId, PeriodType periodType, String periodKey, Integer rank, double score) {
        this.id = UUID.randomUUID().toString();
        this.userId = userId;
        this.categoryId = categoryId;
        this.periodType = periodType;
        this.periodKey = periodKey;
        this.rank = rank;
        this.score = score;
    }
}
