package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.util.badWords.BadWordsFilter;
import com.swmStrong.demo.domain.group.dto.CreateGroupDto;
import com.swmStrong.demo.domain.group.dto.GroupListResponseDto;
import com.swmStrong.demo.domain.group.dto.UpdateGroupDto;
import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.group.repository.GroupRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.domain.userGroup.entity.UserGroup;
import com.swmStrong.demo.domain.userGroup.repository.UserGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService{

    private final GroupRepository groupRepository;
    private final UserInfoProvider userInfoProvider;
    private final UserGroupRepository userGroupRepository;

    public GroupServiceImpl(GroupRepository groupRepository, UserInfoProvider userInfoProvider, UserGroupRepository userGroupRepository) {
        this.groupRepository = groupRepository;
        this.userInfoProvider = userInfoProvider;
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    public void createGroup(String userId, CreateGroupDto createGroupDto) {
        if (!validateGroupName(createGroupDto.groupName())) {
            throw new ApiException(ErrorCode.GROUP_NAME_ALREADY_EXISTS);
        }

        Group group = Group.builder()
                .owner(userInfoProvider.loadByUserId(userId))
                .name(createGroupDto.groupName())
                .tags(createGroupDto.tags())
                .groundRule(createGroupDto.groundRule())
                .description(createGroupDto.description())
                .isPublic(createGroupDto.isPublic())
                .build();

        groupRepository.save(group);
    }

    @Override
    public List<GroupListResponseDto> getGroups() {
        List<Group> groupList = groupRepository.findAll();
        return groupList.stream()
                .map(GroupListResponseDto::of)
                .toList();
    }

    @Transactional
    @Override
    public void joinGroup(String userId, String groupId) {
        User user = userInfoProvider.loadByUserId(userId);
        Group group = groupRepository.findById(Long.parseLong(groupId))
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        if (userGroupRepository.existsByUserAndGroup(user, group)) {
            throw new ApiException(ErrorCode.GROUP_ALREADY_JOINED);
        }

        UserGroup userGroup = new UserGroup(user, group);
        userGroupRepository.save(userGroup);
    }

    @Override
    public void updateGroup(String userId, Long groupId, UpdateGroupDto updateGroupDto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        if (!group.getOwner().getId().equals(userId)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_ONLY);
        }

        if (!group.getName().equals(updateGroupDto.name()) && !validateGroupName(updateGroupDto.name())) {
            throw new ApiException(ErrorCode.GROUP_NAME_ALREADY_EXISTS);
        }

        if (updateGroupDto.description() != null) {
            group.updateDescription(updateGroupDto.description());
        }
        if (updateGroupDto.groundRule() != null) {
            group.updateGroundRule(updateGroupDto.groundRule());
        }
        if (updateGroupDto.name() != null) {
            group.updateName(updateGroupDto.name());
        }

        if  (updateGroupDto.tags() != null) {
            group.updateTags(updateGroupDto.tags());
        }

        if (updateGroupDto.isPublic() != null) {
            group.updateIsPublic(updateGroupDto.isPublic());
        }

        groupRepository.save(group);
    }

    @Override
    public boolean validateGroupName(String groupName) {
        if (BadWordsFilter.isBadWord(groupName)) {
            throw new ApiException(ErrorCode.BAD_WORD_FILTER);
        }
        return !groupRepository.existsByName(groupName);
    }
}
