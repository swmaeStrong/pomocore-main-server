package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.domain.group.dto.*;
import com.swmStrong.demo.domain.group.entity.Group;

import java.time.LocalDate;
import java.util.List;

public interface GroupService {
    void createGroup(String userId, CreateGroupDto createGroupDto);
    void updateNewPassword(String userId, Long groupId);
    void banMember(String userId, Long groupId, BanMemberDto banMemberDto);
    void authorizeOwner(String userId, Long groupId, AuthorizeMemberDto authorizeMemberDto);
    List<GroupResponseDto> getGroups();
    GroupDetailsDto getGroupDetails(Long groupId);
    List<GroupResponseDto> getMyGroups(String userId);
    void joinGroup(String userId, Long groupId, PasswordRequestDto passwordRequestDto);
    void quitGroup(String userId, Long groupId);
    void updateGroup(String userId, Long groupId, UpdateGroupDto updateGroupDto);
    void deleteGroup(String userId, Long groupId);
    boolean validateGroupName(String groupName);
    void setGroupGoal(String userId, Long groupId, SaveGroupGoalDto saveGroupGoalDto);
    List<GroupGoalResponseDto> getGroupGoals(Long groupId, LocalDate date);
    void deleteGroupGoal(String userId, Long groupId, DeleteGroupGoalDto deleteGroupGoalDto);
    GroupLeaderboardDto getGroupLeaderboard(Long groupId, String category, String period, LocalDate date);
    LinkResponseDto createInvitationLink(String userId, Long groupId, EmailsRequestDto emailsRequestDto);
    void joinInvitationLink(String userId, String code);
    GroupResponseDto getGroupByInvitationCode(String code);
    Group getGroupById(Long groupId);
}
