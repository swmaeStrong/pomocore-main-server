package com.swmStrong.demo.domain.sessionScore.entity;

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
public class SessionScore {
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
