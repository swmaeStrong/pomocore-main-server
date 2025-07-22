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
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        int score = 0;
        if (!pomodoroUsageLogList.isEmpty()) {
            score = calculatePomodoroScore(pomodoroUsageLogList, pomodoroUsageLogList.get(0).getSessionMinutes(), event.userId(), event.session(), event.sessionDate());
        }

        PomodoroUsageLog first = pomodoroUsageLogList.get(0);
        PomodoroUsageLog last = pomodoroUsageLogList.get(pomodoroUsageLogList.size() - 1);

        SessionScore sessionScore = SessionScore.builder()
                .title("")
                .score(score)
                .session(event.session())
                .sessionDate(event.sessionDate())
                .user(userInfoProvider.loadByUserId(event.userId()))
                .timestamp(first.getTimestamp())
                .duration(last.getDuration()+last.getTimestamp()-first.getTimestamp())
                .build();

        sessionScoreRepository.save(sessionScore);
    }

    private int calculatePomodoroScore(List<PomodoroUsageLog> pomodoroUsageLogList, int sessionMinutes, String userId, int session, LocalDate sessionDate) {
        // 뭐 한게 없으면 0점
        if (pomodoroUsageLogList == null || pomodoroUsageLogList.isEmpty()) {
            return 0;
        }

        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategoryType.getAllValues();

        // 방해 받은 적이 없다면 일단 100점
        boolean hasDisturb = pomodoroUsageLogList.stream()
                .anyMatch(log -> !workCategories.contains(categoryMap.get(log.getCategoryId())));
        
        if (!hasDisturb) {
            return 100;
        }

        // 점수 계산 시작
        double totalScore = 0;
        // 이전 세션 점수의 영향을 줄임: 55 + (이전점수-55) * 0.15
        // 예: 이전 100점 → 55 + 45*0.15 = 61.75점에서 시작
        SessionScore prevSessionScore = sessionScoreRepository.findByUserIdAndSessionAndSessionDate(userId, session, sessionDate);
        double previousSessionScore = 55; // 기본값을 55로 설정
        if (prevSessionScore != null) {
            previousSessionScore = prevSessionScore.getScore();
        }

        double currentFocusLevel = 55 + (previousSessionScore - 55) * 0.15;
        double lastTimestamp = 0;
        double totalPenalty = 0;
        double continuousFocusTime = 0;
        double maxContinuousFocusTime = 0;
        int disturbCount = 0;

        for (PomodoroUsageLog log : pomodoroUsageLogList) {
            String category = categoryMap.get(log.getCategoryId());
            boolean isWorkCategory = workCategories.contains(category);

            if (isWorkCategory) {
                // 작업 카테고리인 경우 - 계속 집중
                continuousFocusTime += log.getDuration() / 60.0;
            } else {
                // 방해 받은 경우
                // 먼저 방해 전까지의 집중 점수 계산
                double focusDuration = (log.getTimestamp() - lastTimestamp) / 60.0;
                if (focusDuration > 0) {
                    totalScore += calculateFocusScore(currentFocusLevel, focusDuration);
                    currentFocusLevel = Math.min(100, currentFocusLevel + 2.67 * focusDuration);
                    
                    // 최대 연속 집중 시간 업데이트
                    maxContinuousFocusTime = Math.max(maxContinuousFocusTime, continuousFocusTime);
                }

                // 단계별 패널티 적용
                disturbCount++;
                double disturbDuration = log.getDuration();
                totalPenalty += calculateDisturbPenalty(disturbDuration, disturbCount);

                // 다음 구간을 위한 초기화
                lastTimestamp = log.getTimestamp() + log.getDuration();
                continuousFocusTime = 0;
            }
        }

        // 마지막 집중 구간 처리
        double remainingMinutes = sessionMinutes - (lastTimestamp / 60.0);
        if (remainingMinutes > 0) {
            totalScore += calculateFocusScore(currentFocusLevel, remainingMinutes);
            // 마지막 구간의 최대 연속 집중 시간 업데이트
            maxContinuousFocusTime = Math.max(maxContinuousFocusTime, continuousFocusTime + remainingMinutes);
        }

        // 최대 연속 집중 시간 기반 보너스 계산
        double bonusPoints = calculateMaxFocusBonus(maxContinuousFocusTime);

        // 최종 점수 = (총점/세션시간) + 보너스 - 패널티
        int finalScore = (int) Math.max(0, (totalScore / sessionMinutes) + bonusPoints - totalPenalty);
        
        log.info("Session score calculated: totalScore={}, bonus={}, penalty={}, final={}, maxFocus={}min", 
                totalScore/sessionMinutes, bonusPoints, totalPenalty, finalScore, maxContinuousFocusTime);
        
        return finalScore;
    }

    private double calculateFocusScore(double startFocusLevel, double durationMinutes) {
        if (durationMinutes <= 0) {
            return 0;
        }
        // 기준점 60점으로 상향 + 15분동안 집중 시 100점.
        // 더 빠른 집중도 상승으로 짧은 집중도 높은 점수
        double endFocusLevel = Math.min(100, startFocusLevel + 2.67 * durationMinutes);
        double averageFocusLevel = (startFocusLevel + endFocusLevel) / 2;

        return averageFocusLevel * durationMinutes;
    }

    private double calculateDisturbPenalty(double disturbDurationSeconds, int disturbCount) {
        // 시간 비례 패널티: 1초에 -0.05점, 1분에 -8점으로 증가
        // 짧은 방해는 미미하게, 긴 방해는 확실하게
        double timePenalty;
        if (disturbDurationSeconds <= 5) {
            // 5초 이하는 작은 패널티 (1초 = 0.05점)
            timePenalty = disturbDurationSeconds * 0.05;
        } else if (disturbDurationSeconds <= 30) {
            // 5~30초는 점진적 증가
            timePenalty = 0.25 + (disturbDurationSeconds - 5) * 0.08;
        } else {
            // 30초 이상은 가파른 증가 (1분 = 8점)
            timePenalty = 2.25 + (disturbDurationSeconds - 30) * 0.12;
        }
        
        // 방해 횟수에 따른 추가 패널티: 더 강하게 (1번째 1.0, 2번째 1.6, 3번째 2.2, 4번째 2.8...)
        double countMultiplier = 1.0 + (disturbCount - 1) * 0.6;
        
        return timePenalty * countMultiplier;
    }

    private double calculateMaxFocusBonus(double maxContinuousFocusMinutes) {
        // 최대 연속 집중 시간 기반 보너스: 
        // 25분(완벽) = 18점, 20분 = 15점, 15분 = 11점, 12.5분 = 10.5점, 10분 = 9점, 5분 = 7점
        if (maxContinuousFocusMinutes >= 25) {
            return 18; // 완벽한 집중에 대한 최고 보너스
        } else if (maxContinuousFocusMinutes >= 20) {
            return 18;
        } else if (maxContinuousFocusMinutes >= 15) {
            return 11;
        } else if (maxContinuousFocusMinutes >= 12.5) {
            return 10.5;
        } else if (maxContinuousFocusMinutes >= 10) {
            return 9;
        } else if (maxContinuousFocusMinutes >= 5) {
            return 7;
        } else {
            return 0;
        }
    }
}