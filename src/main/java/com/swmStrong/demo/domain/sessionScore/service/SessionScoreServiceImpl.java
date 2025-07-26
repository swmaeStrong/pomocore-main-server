package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategoryType;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.facade.PomodoroSessionProvider;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public List<SessionScoreResponseDto> get(String userId, LocalDate date) {
        List<SessionScore> sessionScoreList = sessionScoreRepository.findByUserIdAndSessionDate(userId, date);
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategoryType.getAllValues();

        return sessionScoreList.stream()
                .map(sessionScore -> {
                    // 해당 세션의 상세 데이터 가져오기
                    List<PomodoroUsageLog> sessionLogs = pomodoroSessionProvider
                            .loadByUserIdAndSessionAndSessionDate(userId, sessionScore.getSession(), sessionScore.getSessionDate());

                    // 상세 정보 생성
                    List<SessionScoreResponseDto.SessionDetailDto> details = sessionLogs.stream()
                            .map(log -> new SessionScoreResponseDto.SessionDetailDto(
                                    convertCategory(categoryMap.getOrDefault(log.getCategoryId(), "Unknown"), workCategories),
                                    categoryMap.getOrDefault(log.getCategoryId(), "Unknown"),
                                    log.getTimestamp(),
                                    log.getDuration()
                            )).toList();

                    int scoreByDistractedCount = (int) Math.pow(2, (double) sessionScore.getDistractedCount()/2);
                    int scoreByDistractedSeconds = sessionScore.getDistractedSeconds() / 10 * 2;
                    int score = 100 - scoreByDistractedSeconds - scoreByDistractedCount;

                    return SessionScoreResponseDto.builder()
                            .session(sessionScore.getSession())
                            .sessionDate(sessionScore.getSessionDate())
                            .distractedCount(sessionScore.getDistractedCount())
                            .distractedSeconds(sessionScore.getDistractedSeconds())
                            .distractedCountScore(scoreByDistractedCount)
                            .distractedSecondsScore(scoreByDistractedSeconds)
                            .score(score)
                            .title(sessionScore.getTitle())
                            .timestamp(sessionScore.getTimestamp())
                            .duration(sessionScore.getDuration())
                            .details(details)
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
}
