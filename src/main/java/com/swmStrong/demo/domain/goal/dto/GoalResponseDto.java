package com.swmStrong.demo.domain.goal.dto;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import lombok.Builder;

@Builder
public record GoalResponseDto(
        String category,
        int currentSeconds,
        int goalValue,
        PeriodType periodType
) {

}
