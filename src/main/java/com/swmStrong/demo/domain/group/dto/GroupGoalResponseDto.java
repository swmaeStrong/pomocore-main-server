package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.common.enums.PeriodType;

import java.util.List;

public record GroupGoalResponseDto(
        String category,
        int currentSeconds,
        int goalSeconds,
        PeriodType periodType,
        List<GroupMember> uncompletedMembers
) {

}
