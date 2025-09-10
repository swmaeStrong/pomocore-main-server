package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategory;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.facade.PomodoroSessionProvider;
import com.swmStrong.demo.domain.sessionScore.dto.SessionDashboardDto;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.dto.WeeklySessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static net.logstash.logback.argument.StructuredArguments.kv;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
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

    public SessionScoreServiceImpl(
            SessionScoreRepository sessionScoreRepository,
            PomodoroSessionProvider pomodoroSessionProvider,
            CategoryProvider categoryProvider,
            LeaderboardProvider leaderboardProvider,
            SessionStateManager sessionStateManager,
            SessionPollingManager sessionPollingManager
    ) {
        this.sessionScoreRepository = sessionScoreRepository;
        this.pomodoroSessionProvider = pomodoroSessionProvider;
        this.categoryProvider = categoryProvider;
        this.leaderboardProvider = leaderboardProvider;
        this.sessionStateManager = sessionStateManager;
        this.sessionPollingManager = sessionPollingManager;
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
                            Math.min(sessionScore.getSessionMinutes(), sessionScore.getDuration()),
                            sessionScore.getAfkDuration(),
                            sessionScore.getDistractedDuration(),
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
                            Math.min(sessionScore.getSessionMinutes(), sessionScore.getDuration()),
                            sessionScore.getAfkDuration(),
                            sessionScore.getDistractedDuration(),
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

    private int getScore(double duration, double afkDuration, double distractedDuration, double dropOutDuration) {
        double distracted = afkDuration + distractedDuration + dropOutDuration;
        return Math.min(100, Math.max(0, 100 - (int) (distracted/duration)));
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
                Math.min(sessionScore.getSessionMinutes(), sessionScore.getDuration()),
                sessionScore.getAfkDuration(),
                sessionScore.getDistractedDuration(),
                (sessionScore.getSessionMinutes() * 60) - sessionScore.getDuration()
        ));

        sessionScoreRepository.save(sessionScore);
        sessionStateManager.markSessionAsProcessed(userId, sessionDate, session);

        List<SessionScoreResponseDto> sessionScores = getByUserIdAndSessionDate(userId, sessionDate);
        sessionPollingManager.notifyAllSessionsProcessed(userId, sessionDate, sessionScores);
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

    @Override
    public WeeklySessionScoreResponseDto getWeeklyDetailsByUserIdAndSessionDate(String userId, LocalDate date) {
        WeekFields weekFields = WeekFields.of(DayOfWeek.MONDAY, 1);
        LocalDate startOfWeek = date.with(weekFields.dayOfWeek(), 1);

        List<SessionScore> sessionScoreList = sessionScoreRepository.findByUserIdAndSessionDateBetween(userId, startOfWeek, date);
        double totalScore = 0;
        for (SessionScore sessionScore: sessionScoreList) {
            int score = getScore(
                    Math.min(sessionScore.getSessionMinutes(), sessionScore.getDuration()),
                    sessionScore.getAfkDuration(),
                    sessionScore.getDistractedDuration(),
                    (sessionScore.getSessionMinutes() * 60) - sessionScore.getDuration()
            );
            totalScore += score;
        }
        return new WeeklySessionScoreResponseDto(totalScore/sessionScoreList.size());
    }

    record Result(
            int distractedCount,
            int distractedDuration,
            double afkDuration
    ) {}
}
