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

                    int score = getScore(sessionScore);

                    return SessionScoreResponseDto.builder()
                            .session(sessionScore.getSession())
                            .sessionMinutes(sessionScore.getSessionMinutes())
                            .sessionDate(sessionScore.getSessionDate())
                            .score(score)
                            .title(sessionScore.getTitle())
                            .titleEng(sessionScore.getTitleEng())
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
                    int score = getScore(sessionScore);

                    return SessionDashboardDto.builder()
                            .session(sessionScore.getSession())
                            .sessionMinutes(sessionScore.getSessionMinutes())
                            .score(score)
                            .title(sessionScore.getTitle())
                            .titleEng(sessionScore.getTitleEng())
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

    private int getScore(SessionScore sessionScore) {
        double dropOutDuration = Math.max(0, sessionScore.getSessionMinutes()*60 - 5 - sessionScore.getDuration());
        double distracted = sessionScore.getAfkDuration() + sessionScore.getDistractedDuration() + dropOutDuration;
        double sessionMinutes = sessionScore.getSessionMinutes() * 60;

        log.debug("distracted: {}", distracted);
        log.debug("sessionMinutes: {}", sessionMinutes);

        return Math.min(100, Math.max(0, (int) ((sessionMinutes-distracted) * 100 /sessionMinutes)));
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

        leaderboardProvider.increaseSessionScore(userId, sessionDate, getScore(sessionScore));

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
        LocalDate startOfWeek = date.with(weekFields.dayOfWeek(), 1).minusDays(1);
        LocalDate endOfWeek = date.with(weekFields.dayOfWeek(), 7).plusDays(1);

        List<SessionScore> sessionScoreList = sessionScoreRepository.findByUserIdAndSessionDateBetween(userId, startOfWeek, endOfWeek);
        if (!sessionScoreList.isEmpty()) {
            double totalScore = 0;
            for (SessionScore sessionScore: sessionScoreList) {
                int score = getScore(sessionScore);
                totalScore += score;
            }
            return new WeeklySessionScoreResponseDto(totalScore/sessionScoreList.size());
        }
        return new WeeklySessionScoreResponseDto(0);
    }

    record Result(
            int distractedCount,
            int distractedDuration,
            double afkDuration
    ) {}
}
