package com.swmStrong.demo.domain.group.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.group.dto.*;
import com.swmStrong.demo.domain.group.entity.Group;
import com.swmStrong.demo.domain.group.repository.GroupRepository;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.domain.user.dto.OnlineRequestDto;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.domain.userGroup.entity.UserGroup;
import com.swmStrong.demo.domain.userGroup.repository.UserGroupRepository;
import com.swmStrong.demo.infra.mail.MailSender;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupServiceImpl 테스트")
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserInfoProvider userInfoProvider;

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private CategoryProvider categoryProvider;

    @Mock
    private LeaderboardProvider leaderboardProvider;

    @Mock
    private GroupAuthorizationProvider groupAuthorizationProvider;

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private GroupServiceImpl groupService;

    private User testUser;
    private User testOwner;
    private Group testGroup;
    private String userId;
    private String ownerId;
    private Long groupId;

    @BeforeEach
    void setUp() {
        userId = "user123";
        ownerId = "owner123";
        groupId = 1L;

        testUser = new User(userId, "테스트사용자");
        testUser.updateRole(Role.USER);

        testOwner = new User(ownerId, "그룹소유자");
        testOwner.updateRole(Role.USER);

        testGroup = Group.builder()
                .owner(testOwner)
                .name("테스트그룹")
                .tags(Arrays.asList("개발", "스터디"))
                .groundRule("규칙을 지켜주세요")
                .description("테스트용 그룹입니다")
                .isPublic(true)
                .password("123456")
                .build();
    }

    @Test
    @DisplayName("그룹 생성 성공")
    void shouldCreateGroup() {
        // given
        CreateGroupDto createDto = new CreateGroupDto(
                "새그룹",
                true,
                "그룹 규칙",
                Arrays.asList("태그1", "태그2"),
                "그룹 설명"
        );

        when(groupRepository.existsByName("새그룹")).thenReturn(false);
        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        groupService.createGroup(userId, createDto);

        // then
        verify(groupRepository).save(any(Group.class));
        verify(userGroupRepository).save(any(UserGroup.class));
    }

    @Test
    @DisplayName("그룹 생성 실패 - 중복된 그룹명")
    void shouldFailCreateGroupWithDuplicateName() {
        // given
        CreateGroupDto createDto = new CreateGroupDto(
                "중복그룹",
                true,
                "규칙",
                Arrays.asList("태그1"),
                "설명"
        );

        when(groupRepository.existsByName("중복그룹")).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> groupService.createGroup(userId, createDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_NAME_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("그룹 비밀번호 업데이트")
    void shouldUpdateGroupPassword() {
        // given
        GroupContext context = new GroupContext(testOwner, testGroup);
        when(groupAuthorizationProvider.authorize(ownerId, groupId, GroupAuthorizationProvider.AuthorizationLevel.OWNER))
                .thenReturn(context);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        groupService.updateNewPassword(ownerId, groupId);

        // then
        verify(groupRepository).save(any(Group.class));
    }

    @Test
    @DisplayName("멤버 추방 성공")
    void shouldBanMember() {
        // given
        BanMemberDto banDto = new BanMemberDto(userId, "테스트 사유");
        GroupContext context = new GroupContext(testOwner, testGroup);
        UserGroup userGroup = new UserGroup(testUser, testGroup);

        when(groupAuthorizationProvider.authorize(ownerId, groupId, GroupAuthorizationProvider.AuthorizationLevel.OWNER))
                .thenReturn(context);
        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);
        when(userGroupRepository.findByUserAndGroup(testUser, testGroup)).thenReturn(Optional.of(userGroup));

        // when
        groupService.banMember(ownerId, groupId, banDto);

        // then
        verify(userGroupRepository).delete(userGroup);
        verify(groupRepository).save(testGroup);
    }

    @Test
    @DisplayName("멤버 추방 실패 - 소유자 자기 자신 추방 시도")
    void shouldFailBanOwnerSelf() {
        // given
        BanMemberDto banDto = new BanMemberDto(ownerId, "자기 자신 추방 시도");
        GroupContext context = new GroupContext(testOwner, testGroup);

        when(groupAuthorizationProvider.authorize(ownerId, groupId, GroupAuthorizationProvider.AuthorizationLevel.OWNER))
                .thenReturn(context);
        when(userInfoProvider.loadByUserId(ownerId)).thenReturn(testOwner);

        // when & then
        assertThatThrownBy(() -> groupService.banMember(ownerId, groupId, banDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_OWNER_CANT_QUIT);
    }

    @Test
    @DisplayName("소유자 권한 위임 성공")
    void shouldAuthorizeNewOwner() {
        // given
        AuthorizeMemberDto authorizeDto = new AuthorizeMemberDto(userId);
        GroupContext context = new GroupContext(testOwner, testGroup);

        when(groupAuthorizationProvider.authorize(ownerId, groupId, GroupAuthorizationProvider.AuthorizationLevel.OWNER))
                .thenReturn(context);
        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);

        // when
        groupService.authorizeOwner(ownerId, groupId, authorizeDto);

        // then
        verify(groupRepository).save(testGroup);
    }

    @Test
    @DisplayName("소유자 권한 위임 실패 - UNREGISTERED 사용자")
    void shouldFailAuthorizeUnregisteredUser() {
        // given
        User unregisteredUser = new User("unregistered", "미등록사용자");
        unregisteredUser.updateRole(Role.UNREGISTERED);

        AuthorizeMemberDto authorizeDto = new AuthorizeMemberDto("unregistered");
        GroupContext context = new GroupContext(testOwner, testGroup);

        when(groupAuthorizationProvider.authorize(ownerId, groupId, GroupAuthorizationProvider.AuthorizationLevel.OWNER))
                .thenReturn(context);
        when(userInfoProvider.loadByUserId("unregistered")).thenReturn(unregisteredUser);

        // when & then
        assertThatThrownBy(() -> groupService.authorizeOwner(ownerId, groupId, authorizeDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode._BAD_REQUEST);
    }

    @Test
    @DisplayName("그룹 상세정보 조회")
    void shouldGetGroupDetails() {
        // given
        testGroup.increaseMemberCount();
        testGroup.increaseMemberCount();

        List<GroupMember> members = Arrays.asList(
                GroupMember.of(testOwner),
                GroupMember.of(testUser)
        );

        Map<String, OnlineRequestDto> onlineDetails = Map.of(
                ownerId, new OnlineRequestDto(1640995200.0, 1),
                userId, new OnlineRequestDto(1640995300.0, 0)
        );

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(userGroupRepository.findByGroupWithUser(testGroup)).thenReturn(members);
        when(userInfoProvider.getUserOnlineDetails(anyList())).thenReturn(onlineDetails);

        // when
        GroupDetailsDto result = groupService.getGroupDetails(groupId);

        // then
        assertThat(result.name()).isEqualTo("테스트그룹");
        assertThat(result.members()).hasSize(2);
    }

    @Test
    @DisplayName("그룹 상세정보 조회 실패 - 그룹이 존재하지 않음")
    void shouldFailGetGroupDetailsNotFound() {
        // given
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> groupService.getGroupDetails(groupId))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_NOT_FOUND);
    }

    @Test
    @DisplayName("모든 공개 그룹 조회")
    void shouldGetAllPublicGroups() {
        // given
        List<Group> groups = Arrays.asList(testGroup);
        when(groupRepository.findAll()).thenReturn(groups);

        // when
        List<GroupResponseDto> result = groupService.getGroups();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("테스트그룹");
    }

    @Test
    @DisplayName("내가 속한 그룹 조회")
    void shouldGetMyGroups() {
        // given
        List<UserGroup> userGroups = Arrays.asList(
                new UserGroup(testUser, testGroup)
        );

        // when
        List<GroupResponseDto> result = groupService.getMyGroups(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("테스트그룹");
    }

    @Test
    @DisplayName("그룹 가입 성공 - 공개 그룹")
    void shouldJoinPublicGroup() {
        // given
        PasswordRequestDto passwordDto = new PasswordRequestDto(null);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);
        when(userGroupRepository.existsByUserAndGroup(testUser, testGroup)).thenReturn(false);

        // when
        groupService.joinGroup(userId, groupId, passwordDto);

        // then
        verify(userGroupRepository).save(any(UserGroup.class));
        verify(groupRepository).save(testGroup);
    }

    @Test
    @DisplayName("그룹 가입 실패 - 이미 가입된 멤버")
    void shouldFailJoinGroupAlreadyMember() {
        // given
        PasswordRequestDto passwordDto = new PasswordRequestDto(null);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);
        when(userGroupRepository.existsByUserAndGroup(testUser, testGroup)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> groupService.joinGroup(userId, groupId, passwordDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_ALREADY_JOINED);
    }

    @Test
    @DisplayName("그룹 탈퇴 성공")
    void shouldQuitGroup() {
        // given
        UserGroup userGroup = new UserGroup(testUser, testGroup);
        testGroup.increaseMemberCount();

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);
        when(userGroupRepository.findByUserAndGroup(testUser, testGroup)).thenReturn(Optional.of(userGroup));

        // when
        groupService.quitGroup(userId, groupId);

        // then
        verify(userGroupRepository).delete(userGroup);
        verify(groupRepository).save(testGroup);
    }

    @Test
    @DisplayName("그룹 탈퇴 실패 - 소유자는 탈퇴 불가")
    void shouldFailQuitGroupAsOwner() {
        // given
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));
        when(userInfoProvider.loadByUserId(ownerId)).thenReturn(testOwner);

        // when & then
        assertThatThrownBy(() -> groupService.quitGroup(ownerId, groupId))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_OWNER_CANT_QUIT);
    }

    @Test
    @DisplayName("그룹명 유효성 검증 - 사용 가능")
    void shouldValidateGroupNameAvailable() {
        // given
        when(groupRepository.existsByName("사용가능한이름")).thenReturn(false);

        // when
        boolean result = groupService.validateGroupName("사용가능한이름");

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("그룹명 유효성 검증 - 이미 존재")
    void shouldValidateGroupNameExists() {
        // given
        when(groupRepository.existsByName("이미존재하는이름")).thenReturn(true);

        // when
        boolean result = groupService.validateGroupName("이미존재하는이름");

        // then
        assertThat(result).isFalse();
    }
}