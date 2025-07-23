package com.swmStrong.demo.domain.streak.entity;

import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_streak")
public class Streak {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "current_streak")
    private int currentStreak = 0;

    @Column(name = "max_streak")
    private int maxStreak = 0;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Builder
    public Streak(User user) {
        this.user = user;
    }

    public void resetCurrentStreak() {
        this.currentStreak = 0;
    }

    public void plusStreak() {
        this.maxStreak = Math.max(this.maxStreak, ++this.currentStreak);
    }

    public void renewLastActiveDate(LocalDate date) {
        this.lastActiveDate = date;
    }
}
