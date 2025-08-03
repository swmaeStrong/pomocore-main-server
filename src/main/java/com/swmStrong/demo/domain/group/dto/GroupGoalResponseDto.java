package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupGoalResponseDto(
        String category,
        int goalSeconds,
        PeriodType periodType,
        List<GroupMemberGoalResult> members
) {

}
