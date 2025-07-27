package com.swmStrong.demo.domain.sessionScore.dto;

import lombok.Builder;

@Builder
public record SessionDashboardDto(
        int session,
        int sessionMinutes,
        int score,
        String title,
        double timestamp,
        double duration
) {}