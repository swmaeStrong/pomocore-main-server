package com.swmStrong.demo.domain.sessionScore.entity;

import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Table(name = "session_score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class SessionScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    private LocalDate sessionDate;

    private int session;

    private int score;

    private String title;

    private double timestamp;

    private double duration;

    @Builder
    public SessionScore(User user, int score, String title, double timestamp, double duration, LocalDate sessionDate, int session) {
        this.user = user;
        this.score = score;
        this.title = title;
        this.timestamp = timestamp;
        this.duration = duration;
        this.sessionDate = sessionDate;
        this.session = session;
    }
}
