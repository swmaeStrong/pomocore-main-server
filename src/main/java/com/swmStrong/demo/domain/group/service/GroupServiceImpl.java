package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.util.badWords.BadWordsFilter;
import com.swmStrong.demo.domain.group.dto.*;
import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.group.repository.GroupRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.domain.userGroup.entity.UserGroup;
import com.swmStrong.demo.domain.userGroup.repository.UserGroupRepository;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class GroupServiceImpl implements GroupService{

    private final GroupRepository groupRepository;
    private final UserInfoProvider userInfoProvider;
    private final UserGroupRepository userGroupRepository;
    private final RedisRepository redisRepository;

    private static final String GROUP_GOAL_PREFIX = "group_goal";

    public GroupServiceImpl(
            GroupRepository groupRepository,
            UserInfoProvider userInfoProvider,
            UserGroupRepository userGroupRepository,
            RedisRepository redisRepository
    ) {
        this.groupRepository = groupRepository;
        this.userInfoProvider = userInfoProvider;
        this.userGroupRepository = userGroupRepository;
        this.redisRepository = redisRepository;
    }

    @Transactional
    @Override
    public void createGroup(String userId, CreateGroupDto createGroupDto) {
        if (!validateGroupName(createGroupDto.name())) {
            throw new ApiException(ErrorCode.GROUP_NAME_ALREADY_EXISTS);
        }

        User user = userInfoProvider.loadByUserId(userId);

        Group group = Group.builder()
                .owner(userInfoProvider.loadByUserId(userId))
                .name(createGroupDto.name())
                .tags(createGroupDto.tags())
                .groundRule(createGroupDto.groundRule())
                .description(createGroupDto.description())
                .isPublic(createGroupDto.isPublic())
                .build();

        groupRepository.save(group);

        UserGroup userGroup = new UserGroup(user, group);
        userGroupRepository.save(userGroup);
    }

    @Override
    public void banMember(String userId, Long groupId, BanMemberDto banMemberDto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));
        User user = userInfoProvider.loadByUserId(userId);

        if (!group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_ONLY);
        }

        User bannedUser = userInfoProvider.loadByUserId(banMemberDto.userId());

        if (bannedUser.equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_CANT_QUIT);
        }

        UserGroup userGroup = userGroupRepository.findByUserAndGroup(bannedUser, group)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_USER_NOT_FOUND));

        userGroupRepository.delete(userGroup);
    }

    @Override
    public void authorizeOwner(String userId, Long groupId, AuthorizeMemberDto authorizeMemberDto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        User user = userInfoProvider.loadByUserId(userId);
        if (!group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_ONLY);
        }

        User newOwner = userInfoProvider.loadByUserId(authorizeMemberDto.userId());

        group.updateOwner(newOwner);

        groupRepository.save(group);
    }

    @Override
    public GroupDetailsDto getGroupDetails(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        List<GroupMember> memberList = userGroupRepository.findByGroup(group).stream()
                .map(userGroup -> GroupMember.builder()
                            .userId(userGroup.getUser().getId())
                            .nickname(userGroup.getUser().getNickname())
                            .build())
                .toList();

        return GroupDetailsDto.of(group, memberList);
    }

    @Override
    public List<GroupListResponseDto> getMyGroups(String userId) {
        User user = userInfoProvider.loadByUserId(userId);

        List<UserGroup> userGroupList = userGroupRepository.findByUser(user);
        return userGroupList.stream()
                .map(userGroup -> GroupListResponseDto.of(userGroup.getGroup()))
                .toList();
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
    public void joinGroup(String userId, Long groupId) {
        User user = userInfoProvider.loadByUserId(userId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        if (userGroupRepository.existsByUserAndGroup(user, group)) {
            throw new ApiException(ErrorCode.GROUP_ALREADY_JOINED);
        }

        //TODO: private 입장 시 확인 절차

        UserGroup userGroup = new UserGroup(user, group);
        userGroupRepository.save(userGroup);
    }

    @Override
    public void quitGroup(String userId, Long groupId) {
        User user = userInfoProvider.loadByUserId(userId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        if (group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_CANT_QUIT);
        }

        userGroupRepository.deleteByUserAndGroup(user, group);
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
    public void deleteGroup(String userId, Long groupId) {
        User user = userInfoProvider.loadByUserId(userId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        if (!group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_ONLY);
        }

        long memberCount = userGroupRepository.countByGroup(group);
        if (memberCount > 1) {
            throw new ApiException(ErrorCode.GROUP_HAS_USER);
        }

        groupRepository.delete(group);
        userGroupRepository.deleteByGroup(group);
    }

    @Override
    public boolean validateGroupName(String name) {
        if (BadWordsFilter.isBadWord(name)) {
            throw new ApiException(ErrorCode.BAD_WORD_FILTER);
        }
        return !groupRepository.existsByName(name);
    }

    @Override
    public void setGroupGoal(String userId, Long groupId, SaveGroupGoalDto saveGroupGoalDto) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new  ApiException(ErrorCode.GROUP_NOT_FOUND));

        User user =  userInfoProvider.loadByUserId(userId);

        if (!group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_ONLY);
        }

        redisRepository.setData(
                generateKey(
                        groupId,
                        saveGroupGoalDto.category(),
                        saveGroupGoalDto.period()
                ),
                saveGroupGoalDto.goalSeconds()
        );
    }

//    @Override
//    public GroupGoalResponseDto getGroupGoal(Long groupId, LocalDate date) {
//        Group group = groupRepository.findById(groupId)
//                .orElseThrow(() -> new  ApiException(ErrorCode.GROUP_NOT_FOUND));
//
//        List<UserGroup> userGroupList = userGroupRepository.findByGroup(group);
//
//        for (UserGroup userGroup: userGroupList) {
//            User user = userGroup.getUser();
//
//        }
//
//        Set<String> goals = redisRepository.findKeys(String.format("%s:%s:*", GROUP_GOAL_PREFIX, groupId));
//
//    }

    private String generateKey(Long groupId, String category, String period) {
        return String.format("%s:%s:%s:%s", GROUP_GOAL_PREFIX, groupId, category, period.toUpperCase());
    }
}
