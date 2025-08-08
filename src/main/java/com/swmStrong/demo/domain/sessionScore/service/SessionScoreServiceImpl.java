package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategory;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.facade.PomodoroSessionProvider;
import com.swmStrong.demo.domain.sessionScore.dto.SessionDashboardDto;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import com.swmStrong.demo.message.event.SessionEndedEvent;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
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

    @EventListener
    public void handleSessionEnded(SessionEndedEvent event) {
        log.info("Session ended event received: userId={}, session={}, sessionDate={}",
                event.userId(), event.session(), event.sessionDate());
        List<PomodoroUsageLog> pomodoroUsageLogList = pomodoroSessionProvider.loadByUserIdAndSessionAndSessionDate(event.userId(), event.session(), event.sessionDate());

        PomodoroUsageLog first = pomodoroUsageLogList.get(0);
        PomodoroUsageLog last = pomodoroUsageLogList.get(pomodoroUsageLogList.size() - 1);
        Result result = calculatePomodoroScore(pomodoroUsageLogList);
        SessionScore sessionScore = sessionScoreRepository.findByUserIdAndSessionAndSessionDate(event.userId(), event.session(), event.sessionDate());

        sessionScore.updateDetails(
                first.getTimestamp(),
                result.afkDuration(),
                last.getDuration()+last.getTimestamp()-first.getTimestamp(),
                result.distractedCount(),
                result.distractedDuration()
        );
        //TODO:여기
        sessionScoreRepository.save(sessionScore);
    }

    /**
     * <h3> 점수 체계에 대한 설명 </h3>
     * 1) 최대 점수는 100점이다. <br>
     * 2) 점수의 감소는 방해로 정의한 사용 로그와 afk 로그에 의해 발생한다. <br>
     * 3) afk와 방해의 횟수와 시간을 합산해 적용한다. <br>
     * 4) 10초 이상의 경우 매 10초마다 2점을 추가로 감점한다. <br>
     * 5) 중간에 탈주한 drop out은 패널티 2배 적용 (대신 count 적용은 하지 않음)
     */
    private Result calculatePomodoroScore(List<PomodoroUsageLog> pomodoroUsageLogList) {
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategory.categories;
        double afkDuration = 0;

        List<PomodoroUsageLog> distractingUsageLog = new ArrayList<>();
        for (PomodoroUsageLog pomodoroUsageLog : pomodoroUsageLogList) {
            if (categoryMap.getOrDefault(pomodoroUsageLog.getCategoryId(), "Unknown").equals("afk")) {
                afkDuration += pomodoroUsageLog.getDuration();
            } else if (!workCategories.contains(categoryMap.getOrDefault(pomodoroUsageLog.getCategoryId(), "Unknown"))) {
                distractingUsageLog.add(pomodoroUsageLog);
            }
        }

        if (distractingUsageLog.isEmpty()) {
            return new Result(0, 0, afkDuration);
        }

        int distractedCount = 0;
        int distractedDuration = 0;
        IntervalEvent[] intervalEvents = new IntervalEvent[2*distractingUsageLog.size()];
        int idx = 0;
        for (PomodoroUsageLog log : distractingUsageLog) {
            intervalEvents[idx++] = new IntervalEvent(log.getTimestamp(), false);
            intervalEvents[idx++] = new IntervalEvent(log.getTimestamp()+log.getDuration() + 5, true);
        }

        Arrays.sort(intervalEvents, Comparator.comparing(IntervalEvent::timestamp)
                .thenComparing(IntervalEvent::isEnd));

        int activeIntervals = 0;
        double lastTimestamp = 0;
        boolean inDisturbInterval = false;

        for (IntervalEvent time : intervalEvents) {
            if (inDisturbInterval) {
                distractedDuration += (int) Math.ceil(time.timestamp - lastTimestamp);
            }

            if (time.isEnd) {
                activeIntervals--;
            } else {
                if (activeIntervals == 0) {
                    distractedCount++;
                }
                activeIntervals++;
            }

            inDisturbInterval = activeIntervals > 0;
            lastTimestamp = time.timestamp;
        }

        return new Result(distractedCount, distractedDuration, afkDuration);
    }

    record IntervalEvent(
            double timestamp,
            boolean isEnd
    ) {}

    record Result(
            int distractedCount,
            int distractedDuration,
            double afkDuration
    ) {}
}
