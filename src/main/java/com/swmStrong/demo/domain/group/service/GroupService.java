package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.domain.group.dto.CreateGroupDto;
import com.swmStrong.demo.domain.group.dto.GroupListResponseDto;
import com.swmStrong.demo.domain.group.dto.UpdateGroupDto;

import java.util.List;

public interface GroupService {
    void createGroup(String userId, CreateGroupDto createGroupDto);
    List<GroupListResponseDto> getGroups();
    void joinGroup(String userId, String groupId);
    void updateGroup(String userId, Long groupId, UpdateGroupDto updateGroupDto);
    boolean validateGroupName(String groupName);
}
