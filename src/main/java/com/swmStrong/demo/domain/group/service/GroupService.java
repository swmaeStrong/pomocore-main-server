package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.group.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface GroupService {
    void createGroup(String userId, CreateGroupDto createGroupDto);
    void banMember(String userId, Long groupId, BanMemberDto banMemberDto);
    void authorizeOwner(String userId, Long groupId, AuthorizeMemberDto authorizeMemberDto);
    List<GroupListResponseDto> getGroups();
    GroupDetailsDto getGroupDetails(Long groupId);
    List<GroupListResponseDto> getMyGroups(String userId);
    void joinGroup(String userId, Long groupId);
    void quitGroup(String userId, Long groupId);
    void updateGroup(String userId, Long groupId, UpdateGroupDto updateGroupDto);
    void deleteGroup(String userId, Long groupId);
    boolean validateGroupName(String groupName);
    void setGroupGoal(String userId, Long groupId, SaveGroupGoalDto saveGroupGoalDto);
    List<GroupGoalResponseDto> getGroupGoals(Long groupId, LocalDate date);
    void deleteGroupGoal(String userId, Long groupId, DeleteGroupGoalDto deleteGroupGoalDto);
    GroupLeaderboardDto getGroupLeaderboard(Long groupId, String category, PeriodType periodType, LocalDate date);
}
