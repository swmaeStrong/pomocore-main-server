package com.swmStrong.demo.domain.sessionScore.listener;

import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategoryType;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.facade.PomodoroSessionProvider;
import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.message.event.SessionEndedEvent;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class SessionScoreEventListener {

    private final PomodoroSessionProvider pomodoroSessionProvider;
    private final SessionScoreRepository sessionScoreRepository;
    private final CategoryProvider categoryProvider;
    private final UserInfoProvider userInfoProvider;

    public SessionScoreEventListener(
            PomodoroSessionProvider pomodoroSessionProvider,
            SessionScoreRepository sessionScoreRepository,
            CategoryProvider categoryProvider,
            UserInfoProvider userInfoProvider
    ) {
        this.pomodoroSessionProvider = pomodoroSessionProvider;
        this.sessionScoreRepository = sessionScoreRepository;
        this.categoryProvider = categoryProvider;
        this.userInfoProvider = userInfoProvider;
    }

    @EventListener
    public void handleSessionEnded(SessionEndedEvent event) {
        log.info("Session ended event received: userId={}, session={}, sessionDate={}",
                event.userId(), event.session(), event.sessionDate());
        List<PomodoroUsageLog> pomodoroUsageLogList = pomodoroSessionProvider.loadByUserIdAndSessionAndSessionDate(event.userId(), event.session(), event.sessionDate());

        PomodoroUsageLog first = pomodoroUsageLogList.get(0);
        PomodoroUsageLog last = pomodoroUsageLogList.get(pomodoroUsageLogList.size() - 1);
        double totalDuration = last.getTimestamp() + last.getDuration() - first.getTimestamp();
        double sessionSeconds = event.sessionMinutes() * 60;
        // 중간에 탈주한 drop out은 패널티 2배 적용 (대신 count 적용은 하지 않음)
        double dropOutTime = Math.max((sessionSeconds - totalDuration - 10) * 2, 0);
        Result result = calculatePomodoroScore(pomodoroUsageLogList, pomodoroUsageLogList.get(0).getSessionMinutes(), event.userId(), event.session(), event.sessionDate(), dropOutTime);

        SessionScore sessionScore = SessionScore.builder()
                .title("")
                .session(event.session())
                .sessionDate(event.sessionDate())
                .user(userInfoProvider.loadByUserId(event.userId()))
                .timestamp(first.getTimestamp())
                .duration(last.getDuration()+last.getTimestamp()-first.getTimestamp())
                .sessionMinutes(event.sessionMinutes())
                .distractedCount(result.distractedCount())
                .distractedSeconds(result.distractedSeconds())
                .build();

        sessionScoreRepository.save(sessionScore);
    }

    /**
     * <h3> 점수 체계에 대한 설명 </h3>
     * 1) 최대 점수는 100점이다. <br>
     * 2) 점수의 감소는 방해로 정의한 사용 로그와 afk 로그에 의해 발생한다. <br>
     * 3) afk와 방해의 횟수와 시간을 합산해 적용한다. <br>
     * 4) 10초 이상의 경우 매 10초마다 2점을 추가로 감점한다. <br>
     * TODO: 25분 세션에 7분만 하고 끝내면 점수 계산 ( 나머지를 afk 수준으로 계산 )
     */
    private Result calculatePomodoroScore(List<PomodoroUsageLog> pomodoroUsageLogList, int sessionMinutes, String userId, int session, LocalDate sessionDate, double dropOutTime) {
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategoryType.getAllValues();

        List<PomodoroUsageLog> disturbUsageLog = pomodoroUsageLogList.stream()
                .filter(log -> !workCategories.contains(categoryMap.get(log.getCategoryId())))
                .toList();

        if (disturbUsageLog.isEmpty()) {
            return new Result(0, 0);
        }

        int distractedCount = 0;
        int distractedSeconds = (int) dropOutTime;
        IntervalEvent[] intervalEvents = new IntervalEvent[2*disturbUsageLog.size()];
        int idx = 0;
        for (PomodoroUsageLog log : disturbUsageLog) {
            intervalEvents[idx++] = new IntervalEvent(log.getTimestamp(), false);
            intervalEvents[idx++] = new IntervalEvent(log.getTimestamp()+log.getDuration() + 0.5, true);
        }

        Arrays.sort(intervalEvents, Comparator.comparing(IntervalEvent::timestamp)
                .thenComparing(IntervalEvent::isEnd));

        int activeIntervals = 0;
        double lastTimestamp = 0;
        boolean inDisturbInterval = false;
        
        for (IntervalEvent time : intervalEvents) {
            if (inDisturbInterval) {
                distractedSeconds += (int) Math.ceil(time.timestamp - lastTimestamp);
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

        return new Result(distractedCount, distractedSeconds);
    }

    record IntervalEvent(
            double timestamp,
            boolean isEnd
    ) {}

    record Result(
            int distractedCount,
            int distractedSeconds
    ) {}
}