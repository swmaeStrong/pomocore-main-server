package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategoryType;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.facade.PomodoroSessionProvider;
import com.swmStrong.demo.domain.sessionScore.dto.SessionDashboardDto;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SessionScoreServiceImpl implements SessionScoreService {

    private final SessionScoreRepository sessionScoreRepository;
    private final PomodoroSessionProvider pomodoroSessionProvider;
    private final CategoryProvider categoryProvider;

    public SessionScoreServiceImpl(
            SessionScoreRepository sessionScoreRepository,
            PomodoroSessionProvider pomodoroSessionProvider,
            CategoryProvider categoryProvider
    ) {
        this.sessionScoreRepository = sessionScoreRepository;
        this.pomodoroSessionProvider = pomodoroSessionProvider;
        this.categoryProvider = categoryProvider;
    }

    @Override
    public List<SessionScoreResponseDto> getByUserIdAndSessionDate(String userId, LocalDate date) {
        List<SessionScore> sessionScoreList = sessionScoreRepository.findAllByUserIdAndSessionDate(userId, date);
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategoryType.getAllValues();

        Map<Integer, List<PomodoroUsageLog>> sessionLogsMap = sessionScoreList.stream()
                .collect(Collectors.toMap(
                        SessionScore::getSession,
                        sessionScore -> pomodoroSessionProvider
                                .loadByUserIdAndSessionAndSessionDate(userId, sessionScore.getSession(), sessionScore.getSessionDate())
                ));

        return sessionScoreList.stream()
                .map(sessionScore -> {
                    List<PomodoroUsageLog> sessionLogs = sessionLogsMap.get(sessionScore.getSession());

                    List<SessionScoreResponseDto.SessionDetailDto> details = sessionLogs.stream()
                            .map(log -> new SessionScoreResponseDto.SessionDetailDto(
                                    convertCategory(categoryMap.getOrDefault(log.getCategoryId(), "Unknown"), workCategories),
                                    categoryMap.getOrDefault(log.getCategoryId(), "Unknown"),
                                    log.getTimestamp(),
                                    log.getDuration()
                            )).toList();

                    int score = getScore(
                            sessionScore.getAfkDuration(),
                            sessionScore.getDistractedDuration(),
                            sessionScore.getDistractedCount(),
                            (sessionScore.getSessionMinutes() * 60) - sessionScore.getDuration()
                    );

                    return SessionScoreResponseDto.builder()
                            .session(sessionScore.getSession())
                            .sessionMinutes(sessionScore.getSessionMinutes())
                            .sessionDate(sessionScore.getSessionDate())
                            .score(score)
                            .title(sessionScore.getTitle())
                            .timestamp(sessionScore.getTimestamp())
                            .duration(sessionScore.getDuration())
                            .details(details)
                            .build();
                }).toList();
    }

    @Override
    public List<SessionDashboardDto> getScoreByUserIdAndSessionDate(String userId, LocalDate date) {
        List<SessionScore> sessionScoreList = sessionScoreRepository.findAllByUserIdAndSessionDate(userId, date);

        return sessionScoreList.stream()
                .map(sessionScore -> {
                    int score = getScore(
                            sessionScore.getAfkDuration(),
                            sessionScore.getDistractedDuration(),
                            sessionScore.getDistractedCount(),
                            (sessionScore.getSessionMinutes() * 60) - sessionScore.getDuration()
                    );

                    return SessionDashboardDto.builder()
                            .session(sessionScore.getSession())
                            .sessionMinutes(sessionScore.getSessionMinutes())
                            .score(score)
                            .title(sessionScore.getTitle())
                            .timestamp(sessionScore.getTimestamp())
                            .duration(sessionScore.getDuration())
                            .build();
                }).toList();
    }

    private String convertCategory(String category, Set<String> workCategories) {
        if (workCategories.contains(category)) {
            return "work";
        } else if (category.equals("AFK")) {
            return "afk";
        } else {
            return "distraction";
        }
    }

    private int getScore(double afkDuration, double distractedDuration, int distractedCount, double dropOutDuration) {
        int score = 100;
        score -= (int) Math.pow(2, (double) distractedCount / 2) - 1;
        score -= (int) distractedDuration / 10 * 2;
        score = Math.max(30, score);

        dropOutDuration = Math.max(0, dropOutDuration - 10);

        score -= (int) dropOutDuration / 10;
        score -= (int) afkDuration / 10 * 2;
        score -= distractedCount / 10;

        score = Math.max(0, score);

        return score;
    }
}
