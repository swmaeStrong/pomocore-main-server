package com.swmStrong.demo.message.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PomodoroPatternClassifyMessage(
    String userId,
    String categorizedDataId,
    String pomodoroUsageLogId,
    String url,
    String title,
    String app,
    int session,
    LocalDate sessionDate,
    int sessionMinutes,
    double duration,
    double timestamp,
    boolean isEnd
) {
}
