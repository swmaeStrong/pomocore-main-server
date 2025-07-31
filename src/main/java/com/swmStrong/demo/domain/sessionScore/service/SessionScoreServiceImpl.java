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
    /// 1. 메인에 나가는 통계
    /// 2. 분석에 나가는 기본 통계
    /// 3. 분석 상호 작용 시 나가는 추가 통계


    /// 1. 메인에 나가는 통계 (detail 표시)
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

                    int scoreByDistractedCount = (int) Math.pow(2, (double) sessionScore.getDistractedCount()/2);
                    int scoreByDistractedSeconds = sessionScore.getDistractedDuration() / 10 * 2;
                    int score = Math.max(30, 100 - scoreByDistractedSeconds - scoreByDistractedCount);
                    score -= (int) sessionScore.getAfkDuration() / 10 * 2;
                    score = Math.max(0, score);
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

    /// 2. 분석에 나가는 기본 통계 (시간과 그 세션에 해당 하는 점수만 제공)
    /// 여기서 session의 distractedDuration, distractedCount를 쥐고 있을 수 있나?
    @Override
    public List<SessionDashboardDto> getScoreByUserIdAndSessionDate(String userId, LocalDate date) {
        List<SessionScore> sessionScoreList = sessionScoreRepository.findAllByUserIdAndSessionDate(userId, date);

        return sessionScoreList.stream()
                .map(sessionScore -> {
                    int scoreByDistractedCount = (int) Math.pow(2, (double) sessionScore.getDistractedCount()/2);
                    int scoreByDistractedSeconds = sessionScore.getDistractedDuration() / 10 * 2;
                    int score = Math.max(30, 100 - scoreByDistractedSeconds - scoreByDistractedCount);
                    score -= (int) sessionScore.getAfkDuration() / 10 * 2;
                    score = Math.max(0, score);
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

    ///  3. 분석 상호 작용 시 나가는 추가 통계 -> usageLog쪽에서 구현

    private String convertCategory(String category, Set<String> workCategories) {
        if (workCategories.contains(category)) {
            return "work";
        } else if (category.equals("AFK")) {
            return "afk";
        } else {
            return "distraction";
        }
    }
}
