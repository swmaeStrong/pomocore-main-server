package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.common.util.badWords.BadWordsFilter;
import com.swmStrong.demo.domain.group.dto.*;
import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.group.repository.GroupRepository;
import com.swmStrong.demo.domain.user.dto.OnlineRequestDto;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.domain.userGroup.entity.UserGroup;
import com.swmStrong.demo.domain.userGroup.repository.UserGroupRepository;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroupServiceImpl implements GroupService{

    private final GroupRepository groupRepository;
    private final UserInfoProvider userInfoProvider;
    private final UserGroupRepository userGroupRepository;
    private final RedisRepository redisRepository;
    private final CategoryProvider categoryProvider;
    private final LeaderboardProvider leaderboardProvider;

    private static final String GROUP_GOAL_PREFIX = "group_goal";

    public GroupServiceImpl(
            GroupRepository groupRepository,
            UserInfoProvider userInfoProvider,
            UserGroupRepository userGroupRepository,
            RedisRepository redisRepository,
            CategoryProvider categoryProvider,
            LeaderboardProvider leaderboardProvider
    ) {
        this.groupRepository = groupRepository;
        this.userInfoProvider = userInfoProvider;
        this.userGroupRepository = userGroupRepository;
        this.redisRepository = redisRepository;
        this.categoryProvider = categoryProvider;
        this.leaderboardProvider = leaderboardProvider;
    }

    @Transactional
    @Override
    public void createGroup(String userId, CreateGroupDto createGroupDto) {
        if (!validateGroupName(createGroupDto.name())) {
            throw new ApiException(ErrorCode.GROUP_NAME_ALREADY_EXISTS);
        }

        String randomPassword = getRandomPassword();

        User user = userInfoProvider.loadByUserId(userId);

        Group group = Group.builder()
                .owner(userInfoProvider.loadByUserId(userId))
                .name(createGroupDto.name())
                .tags(createGroupDto.tags())
                .groundRule(createGroupDto.groundRule())
                .description(createGroupDto.description())
                .isPublic(createGroupDto.isPublic())
                .password(randomPassword)
                .build();

        group.increaseMemberCount();
        groupRepository.save(group);

        UserGroup userGroup = new UserGroup(user, group);
        userGroupRepository.save(userGroup);
    }

    @Override
    public void updateNewPassword(String userId, Long groupId) {
        User user = userInfoProvider.loadByUserId(userId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        if (!group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_ONLY);
        }
        group.updatePassword(getRandomPassword());
        groupRepository.save(group);
    }

    @Override
    @Transactional
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
        group.decreaseMemberCount();
        groupRepository.save(group);
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

        List<GroupMember> members = userGroupRepository.findByGroupWithUser(group);
        
        List<String> userIds = members.stream()
                .map(GroupMember::userId)
                .toList();

        
        Map<String, OnlineRequestDto> onlineDetails = userInfoProvider.getUserOnlineDetails(userIds);
        
        List<GroupMember> membersWithOnlineInfo = members.stream()
                .map(member -> {
                    OnlineRequestDto onlineInfo = onlineDetails.get(member.userId());
                    return GroupMember.builder()
                            .userId(member.userId())
                            .nickname(member.nickname())
                            .profileImageUrl(member.profileImageUrl())
                            .lastActivityTimestamp(onlineInfo != null ? onlineInfo.timestamp() : null)
                            .sessionMinutes(onlineInfo != null ? onlineInfo.sessionMinutes() : null)
                            .build();
                })
                .toList();

        return GroupDetailsDto.of(group, membersWithOnlineInfo);
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
    public void joinGroup(String userId, Long groupId, PasswordRequestDto passwordRequestDto) {
        User user = userInfoProvider.loadByUserId(userId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        if (!group.isPublic()) {
            if (passwordRequestDto == null) {
                throw new ApiException(ErrorCode.PASSWORD_NEEDED);
            }

            if (!group.getPassword().equals(passwordRequestDto.password())) {
                throw new ApiException(ErrorCode.INCORRECT_PASSWORD);
            }
        }

        if (userGroupRepository.existsByUserAndGroup(user, group)) {
            throw new ApiException(ErrorCode.GROUP_ALREADY_JOINED);
        }

        group.increaseMemberCount();
        groupRepository.save(group);

        UserGroup userGroup = new UserGroup(user, group);
        userGroupRepository.save(userGroup);
    }

    @Override
    @Transactional
    public void quitGroup(String userId, Long groupId) {
        User user = userInfoProvider.loadByUserId(userId);
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        if (group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_CANT_QUIT);
        }

        userGroupRepository.deleteByUserAndGroup(user, group);

        group.decreaseMemberCount();
        groupRepository.save(group);
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
    @Transactional
    public void deleteGroup(String userId, Long groupId) {
        User user = userInfoProvider.loadByUserId(userId);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        if (!group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_ONLY);
        }

        if (group.getMemberCount() > 1) {
            throw new ApiException(ErrorCode.GROUP_HAS_USER);
        }

        userGroupRepository.deleteByGroup(group);
        groupRepository.delete(group);
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

        User user = userInfoProvider.loadByUserId(userId);

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

    @Override
    public List<GroupGoalResponseDto> getGroupGoals(Long groupId, LocalDate date) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new  ApiException(ErrorCode.GROUP_NOT_FOUND));

        List<User> groupUserList = userGroupRepository.findByGroup(group).stream()
                .map(UserGroup::getUser)
                .toList();

        List<GroupGoalResponseDto> groupGoalResponseDtoList = new ArrayList<>();

        List<String> groupGoalKeys = new ArrayList<>();
        List<String> categoryList = new ArrayList<>(categoryProvider.getCategories());
        categoryList.add("work");

        for (String category: categoryList) {
            for (PeriodType periodType: PeriodType.values()) {
                String groupGoalKey = generateKey(groupId, category, periodType.toString());
                groupGoalKeys.add(groupGoalKey);
            }
        }

        Map<String, String> groupGoalData = redisRepository.multiGet(groupGoalKeys);
        for (String category: categoryProvider.getCategories()) {
            for (PeriodType periodType: PeriodType.values()) {
                String groupGoalKey = generateKey(groupId, category, periodType.toString());
                String goalValue = groupGoalData.get(groupGoalKey);

                if (goalValue != null) {
                    List<GroupMemberGoalResult> members = new ArrayList<>();

                    for (User user : groupUserList) {
                        double currentSeconds = leaderboardProvider.getUserScore(user.getId(), category, date, periodType);
                        members.add(new GroupMemberGoalResult(user.getId(), currentSeconds));
                    }

                    groupGoalResponseDtoList.add(GroupGoalResponseDto.builder()
                            .category(category)
                            .periodType(periodType)
                            .members(members)
                            .goalSeconds(Integer.parseInt(goalValue))
                            .build());
                }
            }
        }

        return groupGoalResponseDtoList;
    }

    @Override
    public void deleteGroupGoal(String userId, Long groupId, DeleteGroupGoalDto deleteGroupGoalDto) {
        Group group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new  ApiException(ErrorCode.GROUP_NOT_FOUND));
        User user = userInfoProvider.loadByUserId(userId);

        if (!group.getOwner().equals(user)) {
            throw new ApiException(ErrorCode.GROUP_OWNER_ONLY);
        }

        redisRepository.deleteData(generateKey(groupId, deleteGroupGoalDto.category(), deleteGroupGoalDto.period()));
    }

    @Override
    public GroupLeaderboardDto getGroupLeaderboard(Long groupId, String category, String period, LocalDate date) {
        PeriodType periodType = PeriodType.valueOf(period.toUpperCase());

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException(ErrorCode.GROUP_NOT_FOUND));

        List<User> groupUsers = userGroupRepository.findUsersByGroup(group);
        List<GroupLeaderboardMember> members = leaderboardProvider.getGroupLeaderboardMembers(groupUsers, category, date, periodType);

        return GroupLeaderboardDto.builder()
                .category(category)
                .periodType(periodType)
                .date(date)
                .members(members)
                .build();
    }

    private String generateKey(Long groupId, String category, String period) {
        return String.format("%s:%s:%s:%s", GROUP_GOAL_PREFIX, groupId, category, period.toUpperCase());
    }

    private String getRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        return new SecureRandom().ints(6, 0, chars.length())
                .mapToObj(chars::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
