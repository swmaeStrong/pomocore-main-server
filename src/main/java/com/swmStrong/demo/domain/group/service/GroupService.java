package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.domain.group.dto.CreateGroupDto;
import com.swmStrong.demo.domain.group.dto.GroupDetailsDto;
import com.swmStrong.demo.domain.group.dto.GroupListResponseDto;
import com.swmStrong.demo.domain.group.dto.UpdateGroupDto;

import java.util.List;

public interface GroupService {
    void createGroup(String userId, CreateGroupDto createGroupDto);
    List<GroupListResponseDto> getGroups();
    GroupDetailsDto getGroupDetails(Long groupId);
    List<GroupListResponseDto> getMyGroups(String userId);
    void joinGroup(String userId, Long groupId);
    void quitGroup(String userId, Long groupId);
    void updateGroup(String userId, Long groupId, UpdateGroupDto updateGroupDto);
    void deleteGroup(String userId, Long groupId);
    boolean validateGroupName(String groupName);
}
