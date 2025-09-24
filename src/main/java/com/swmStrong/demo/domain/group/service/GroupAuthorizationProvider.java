package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.group.dto.GroupContext;
import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.group.repository.GroupRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.domain.userGroup.repository.UserGroupRepository;
import org.springframework.stereotype.Component;

@Component
public class GroupAuthorizationProvider {

    private final UserInfoProvider userInfoProvider;
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;

    public GroupAuthorizationProvider(
            UserInfoProvider userInfoProvider,
            GroupRepository groupRepository,
            UserGroupRepository userGroupRepository
    ) {
        this.userInfoProvider = userInfoProvider;
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;
    }

    public enum AuthorizationLevel {
        MEMBER, OWNER, GUEST
    }

    public GroupContext authorize(String userId, Long groupId, AuthorizationLevel authorizationLevel) {
        User user = userInfoProvider.loadByUserId(userId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        switch (authorizationLevel) {
            case MEMBER -> validateMembership(user, group);
            case OWNER -> validateOwnership(user, group);
            case GUEST -> validateGuest(user, group);
        }
        return new GroupContext(user, group);
    }

    private void validateMembership(User user, Group group) {
        if (!userGroupRepository.existsByUserAndGroup(user, group)) {
            throw new ApiException(ErrorCode.GROUP_USER_NOT_FOUND);
        }
    }

    private void validateOwnership(User user, Group group) {
        if (!group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_ONLY);
        }
    }

    private void validateGuest(User user, Group group) {
        if (userGroupRepository.existsByUserAndGroup(user, group)) {
            throw new ApiException(ErrorCode.GROUP_ALREADY_JOINED);
        }
    }
}
