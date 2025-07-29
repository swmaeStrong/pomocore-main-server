package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.domain.group.dto.CreateGroupDto;

public interface GroupService {
    void createGroup(String userId, CreateGroupDto createGroupDto);
}
