package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategory;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.facade.PomodoroSessionProvider;
import com.swmStrong.demo.domain.sessionScore.dto.SessionDashboardDto;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import com.swmStrong.demo.infra.LLM.LLMSummaryProvider;
import com.swmStrong.demo.message.event.SessionProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SessionScoreServiceImpl implements SessionScoreService {

    private final SessionScoreRepository sessionScoreRepository;
    private final PomodoroSessionProvider pomodoroSessionProvider;
    private final CategoryProvider categoryProvider;
    private final LeaderboardProvider leaderboardProvider;
    private final SessionStateManager sessionStateManager;
    private final SessionPollingManager sessionPollingManager;
    private final ApplicationEventPublisher eventPublisher;
    private final LLMSummaryProvider llmSummaryProvider;

    public SessionScoreServiceImpl(
            SessionScoreRepository sessionScoreRepository,
            PomodoroSessionProvider pomodoroSessionProvider,
            CategoryProvider categoryProvider,
            LeaderboardProvider leaderboardProvider,
            SessionStateManager sessionStateManager,
            SessionPollingManager sessionPollingManager,
            ApplicationEventPublisher eventPublisher,
            LLMSummaryProvider llmSummaryProvider
    ) {
        this.sessionScoreRepository = sessionScoreRepository;
        this.pomodoroSessionProvider = pomodoroSessionProvider;
        this.categoryProvider = categoryProvider;
        this.leaderboardProvider = leaderboardProvider;
        this.sessionStateManager = sessionStateManager;
        this.sessionPollingManager = sessionPollingManager;
        this.eventPublisher = eventPublisher;
        this.llmSummaryProvider = llmSummaryProvider;
    }

    @Override
    public List<SessionScoreResponseDto> getByUserIdAndSessionDate(String userId, LocalDate date) {
        List<SessionScore> sessionScoreList = sessionScoreRepository.findAllByUserIdAndSessionDate(userId, date);
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategory.categories;

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
                                    convertCategory(categoryMap.getOrDefault(log.getCategoryId(), "Uncategorized"), workCategories),
                                    categoryMap.getOrDefault(log.getCategoryId(), "Uncategorized"),
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
        score -= (int) Math.pow(2, (double) distractedCount / 4) - 1;
        score -= (int) distractedDuration / 10 * 2;
        score = Math.max(30, score);

        dropOutDuration = Math.max(0, dropOutDuration - 10);

        score -= (int) dropOutDuration / 10;
        score -= (int) afkDuration / 10 * 2;
        score -= distractedCount / 10;

        score = Math.max(0, score);

        return score;
    }

    @Transactional
    @Override
    public void processSessionEnded(String userId, int session, LocalDate sessionDate) {
        log.info("Processing session ended",
                kv("service", "pomocore-backend"),
                kv("userId", userId),
                kv("session", session),
                kv("sessionDate", sessionDate.toString()));
        
        List<PomodoroUsageLog> pomodoroUsageLogList = pomodoroSessionProvider.loadByUserIdAndSessionAndSessionDate(userId, session, sessionDate);

        PomodoroUsageLog first = pomodoroUsageLogList.get(0);
        PomodoroUsageLog last = pomodoroUsageLogList.get(pomodoroUsageLogList.size() - 1);
        Result result = calculatePomodoroScore(pomodoroUsageLogList);
        SessionScore sessionScore = sessionScoreRepository.findByUserIdAndSessionAndSessionDate(userId, session, sessionDate);

        sessionScore.updateDetails(
                first.getTimestamp(),
                result.afkDuration(),
                last.getDuration()+last.getTimestamp()-first.getTimestamp(),
                result.distractedCount(),
                result.distractedDuration()
        );

        leaderboardProvider.increaseSessionScore(userId, sessionDate, getScore(
                sessionScore.getAfkDuration(),
                sessionScore.getDistractedDuration(),
                sessionScore.getDistractedCount(),
                (sessionScore.getSessionMinutes() * 60) - sessionScore.getDuration()
        ));

        sessionScoreRepository.save(sessionScore);
        
        // Redis에 세션 처리 완료 상태 저장
        sessionStateManager.markSessionAsProcessed(userId, sessionDate, session);

        // 대기 중인 롱 폴링 요청에 응답
        List<SessionScoreResponseDto> sessionScores = getByUserIdAndSessionDate(userId, sessionDate);
        
        // 모든 세션이 처리되었는지 확인
        boolean allSessionsProcessed = sessionScores.stream()
                .allMatch(sessionData -> sessionStateManager.isSessionProcessed(userId, sessionDate, sessionData.session()));
        
        // 개별 세션에 대한 롱 폴링 알림 (기존 호환성)
        sessionPollingManager.notifySessionProcessed(userId, sessionDate, session, sessionScores);
        
        // 모든 세션이 처리되었을 때만 날짜 기반 롱 폴링에 알림
        if (allSessionsProcessed) {
            sessionPollingManager.notifyAllSessionsProcessed(userId, sessionDate, sessionScores);
        }
        
        // 처리 완료 이벤트 발행 (필요한 경우 다른 컴포넌트에서 사용)
        eventPublisher.publishEvent(SessionProcessedEvent.builder()
                .userId(userId)
                .session(session)
                .sessionDate(sessionDate)
                .sessionScores(sessionScores)
                .build());
    }

    private Result calculatePomodoroScore(List<PomodoroUsageLog> pomodoroUsageLogList) {
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategory.categories;
        double afkDuration = 0;

        List<PomodoroUsageLog> distractingUsageLogList = new ArrayList<>();
        for (PomodoroUsageLog pomodoroUsageLog : pomodoroUsageLogList) {
            if (categoryMap.getOrDefault(pomodoroUsageLog.getCategoryId(), "Uncategorized").equals("afk")) {
                afkDuration += pomodoroUsageLog.getDuration();
            } else if (!workCategories.contains(categoryMap.getOrDefault(pomodoroUsageLog.getCategoryId(), "Uncategorized"))) {
                distractingUsageLogList.add(pomodoroUsageLog);
            }
        }

        if (distractingUsageLogList.isEmpty()) {
            return new Result(0, 0, afkDuration);
        }
        int distractedCount = 0;
        int distractedDuration = 0;
        for (PomodoroUsageLog usageLog: distractingUsageLogList) {
            distractedCount ++;
            distractedDuration += (int) usageLog.getDuration();
        }

        return new Result(distractedCount, distractedDuration, afkDuration);
    }


    record Result(
            int distractedCount,
            int distractedDuration,
            double afkDuration
    ) {}
}
