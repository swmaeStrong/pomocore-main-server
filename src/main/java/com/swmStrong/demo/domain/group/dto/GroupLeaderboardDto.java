package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record GroupLeaderboardDto(
        Long groupId,
        String groupName,
        String category,
        PeriodType periodType,
        LocalDate date,
        List<GroupLeaderboardMember> members
) {
}