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
        double dropOutTime = Math.max((sessionSeconds - totalDuration - 10) * 2, 0);
        Result result = calculatePomodoroScore(pomodoroUsageLogList, dropOutTime);
        SessionScore sessionScore = sessionScoreRepository.findByUserIdAndSessionAndSessionDate(event.userId(), event.session(), event.sessionDate());

        sessionScore.updateDetails(
                first.getTimestamp(),
                result.afkDuration(),
                last.getDuration()+last.getTimestamp()-first.getTimestamp(),
                result.distractedCount(),
                result.distractedDuration()
        );

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
    private Result calculatePomodoroScore(List<PomodoroUsageLog> pomodoroUsageLogList, double dropOutTime) {
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategoryType.getAllValues();
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
        int distractedDuration = (int) dropOutTime;
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