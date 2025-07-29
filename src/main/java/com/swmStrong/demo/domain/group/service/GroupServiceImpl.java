package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.domain.group.dto.CreateGroupDto;
import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.group.repository.GroupRepository;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import org.springframework.stereotype.Service;

@Service
public class GroupServiceImpl implements GroupService{

    private final GroupRepository groupRepository;
    private final UserInfoProvider userInfoProvider;

    public GroupServiceImpl(GroupRepository groupRepository, UserInfoProvider userInfoProvider) {
        this.groupRepository = groupRepository;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public void createGroup(String userId, CreateGroupDto createGroupDto) {
        Group group = Group.builder()
                .user(userInfoProvider.loadByUserId(userId))
                .name(createGroupDto.groupName())
                .tag(createGroupDto.tag())
                .groundRule(createGroupDto.groundRule())
                .description(createGroupDto.description())
                .isPublic(createGroupDto.isPublic())
                .build();

        groupRepository.save(group);
    }

    
}
