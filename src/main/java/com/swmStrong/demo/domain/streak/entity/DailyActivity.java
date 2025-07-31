package com.swmStrong.demo.domain.streak.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Table(name = "daily_activity", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "activity_date"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class DailyActivity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "activity_count", nullable = false)
    private int activityCount = 0;

    @Builder
    public DailyActivity(User user, LocalDate activityDate) {
        this.user = user;
        this.activityDate = activityDate;
    }

    public void increaseActivityCount() {
        this.activityCount++;
    }
}

