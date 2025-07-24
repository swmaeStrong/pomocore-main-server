package com.swmStrong.demo.domain.goal.entity;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;

import java.time.LocalDate;

@Table(name = "goal_result",
        uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "session_date"})
        }
)
@Entity
public class GoalResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @Column(name = "date")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @Column(name = "goal_seconds", nullable = false)
    private int goalSeconds;

    private int achievedSeconds;

    @Builder
    public GoalResult(User user, LocalDate date, PeriodType periodType, int goalSeconds, int achievedSeconds) {
        this.user = user;
        this.date = date;
        this.periodType = periodType;
        this.goalSeconds = goalSeconds;
        this.achievedSeconds = achievedSeconds;
    }
}
