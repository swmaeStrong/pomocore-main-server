package com.swmStrong.demo.domain.sessionScore.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "session_score", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "session_date", "session"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class SessionScore extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "session", nullable = false)
    private int session;

    @Column(name = "session_minutes", nullable = false)
    private int sessionMinutes;

    @Column(name = "distracted_count")
    private int distractedCount;

    @Column(name = "distracted_duration")
    private int distractedDuration;

    @Column(name = "afk_duration")
    private double afkDuration;

    private String title;

    private String titleEng;

    private double timestamp;

    private double duration;

    @Builder
    public SessionScore(User user, double timestamp, double duration, int sessionMinutes,
                        LocalDate sessionDate, int session, int distractedCount, int distractedDuration,
                        double afkDuration
    ) {
        this.user = user;
        this.timestamp = timestamp;
        this.duration = duration;
        this.sessionDate = sessionDate;
        this.session = session;
        this.distractedCount = distractedCount;
        this.distractedDuration = distractedDuration;
        this.sessionMinutes = sessionMinutes;
        this.afkDuration = afkDuration;
    }

    public void updateDetails(double timestamp, double afkDuration, double duration, int distractedCount, int distractedDuration) {
        this.timestamp = timestamp;
        this.afkDuration = afkDuration;
        this.duration = duration;
        this.distractedCount = distractedCount;
        this.distractedDuration = distractedDuration;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateEngTitle(String titleEng) {
        this.titleEng = titleEng;
    }
}
