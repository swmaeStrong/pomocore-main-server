package com.swmStrong.demo.message.dto;

import lombok.Builder;

@Builder
public record PomodoroPatternClassifyMessage(
    String categorizedDataId,
    String pomodoroUsageLogId,
    String url,
    String title,
    String app
) {
}
