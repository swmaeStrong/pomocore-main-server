package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.config.s3.S3Properties;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.common.util.badWords.BadWordsFilter;
import com.swmStrong.demo.domain.streak.entity.Streak;
import com.swmStrong.demo.domain.streak.facade.StreakProvider;
import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import com.swmStrong.demo.infra.token.TokenManager;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl 테스트")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Properties s3Properties;

    @Mock
    private StreakProvider streakProvider;

    @Mock
    private ApplicationContext applicationContext;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private HttpServletRequest request;

    private User testUser;
    private String userId;
    private String nickname;

    @BeforeEach
    void setUp() {
        userId = "test-user-123";
        nickname = "테스트닉네임";
        testUser = new User(userId, nickname);
        testUser.updateRole(Role.USER);
        testUser.updateProfileImage("https://example.com/profile.jpg", "profile-key-123");
    }

    @Test
    @DisplayName("사용자 상세정보 조회 - 스트릭이 있는 경우")
    void shouldGetUserDetailsWithStreak() {
        // given
        Streak streak = Streak.builder()
                .user(testUser)
                .build();
        streak.plusStreak();
        streak.plusStreak();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(redisRepository.getJsonData(anyString(), eq(UserResponseDto.class))).thenReturn(null);
        when(streakProvider.loadStreakByUserId(userId)).thenReturn(streak);
        when(streakProvider.loadTotalSessionByUserId(userId)).thenReturn(100);

        // when
        UserInfoResponseDto result = userService.getDetailsByUserId(userId);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.nickname()).isEqualTo(nickname);
        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.maxStreak()).isEqualTo(2);
        assertThat(result.totalSession()).isEqualTo(100);
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    @DisplayName("사용자 상세정보 조회 - 스트릭이 없는 경우")
    void shouldGetUserDetailsWithoutStreak() {
        // given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(redisRepository.getJsonData(anyString(), eq(UserResponseDto.class))).thenReturn(null);
        when(streakProvider.loadStreakByUserId(userId)).thenReturn(null);
        when(streakProvider.loadTotalSessionByUserId(userId)).thenReturn(null);

        // when
        UserInfoResponseDto result = userService.getDetailsByUserId(userId);

        // then
        assertThat(result.currentStreak()).isEqualTo(0);
        assertThat(result.maxStreak()).isEqualTo(0);
        assertThat(result.totalSession()).isEqualTo(0);
    }

    @Test
    @DisplayName("게스트 회원가입 성공")
    void shouldSignupGuest() {
        // given
        UserRequestDto userRequestDto = new UserRequestDto(userId);
        TokenResponseDto expectedToken = new TokenResponseDto("access-token", "refresh-token");

        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(userRepository.existsById(userId)).thenReturn(false);
        when(redisRepository.incrementWithExpireIfFirst(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(1L);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenManager.getToken(userId, "Test-Agent", Role.UNREGISTERED)).thenReturn(expectedToken);

        // when
        TokenResponseDto result = userService.signupGuest(request, userRequestDto);

        // then
        assertThat(result).isEqualTo(expectedToken);
        verify(userRepository).save(any(User.class));
        verify(redisRepository).setJsonDataWithExpire(anyString(), any(UserResponseDto.class), anyInt());
    }

    @Test
    @DisplayName("게스트 회원가입 실패 - 중복된 사용자 ID")
    void shouldFailSignupGuestWithDuplicateId() {
        // given
        UserRequestDto userRequestDto = new UserRequestDto(userId);
        when(userRepository.existsById(userId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signupGuest(request, userRequestDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USER_ID);
    }

    @Test
    @DisplayName("게스트 회원가입 실패 - IP 제한 초과")
    void shouldFailSignupGuestWithIpRateLimit() {
        // given
        UserRequestDto userRequestDto = new UserRequestDto(userId);
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1");
        when(userRepository.existsById(userId)).thenReturn(false);
        when(redisRepository.incrementWithExpireIfFirst(anyString(), anyLong(), any(TimeUnit.class))).thenReturn(6L);

        // when & then
        assertThatThrownBy(() -> userService.signupGuest(request, userRequestDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.IP_RATE_LIMIT_EXCEEDED);
    }

    @Test
    @DisplayName("닉네임 유효성 검증 성공")
    void shouldValidateNickname() {
        // given
        String newNickname = "새닉네임";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(redisRepository.getJsonData(anyString(), eq(UserResponseDto.class))).thenReturn(null);
        when(userRepository.existsByNickname(newNickname)).thenReturn(false);

        try (MockedStatic<BadWordsFilter> mockedFilter = mockStatic(BadWordsFilter.class)) {
            mockedFilter.when(() -> BadWordsFilter.isBadWord(newNickname)).thenReturn(false);

            // when & then (예외가 발생하지 않으면 성공)
            userService.validateNickname(userId, newNickname);
        }
    }

    @Test
    @DisplayName("닉네임 유효성 검증 실패 - 비속어 포함")
    void shouldFailValidateNicknameWithBadWord() {
        // given
        String badNickname = "비속어닉네임";

        try (MockedStatic<BadWordsFilter> mockedFilter = mockStatic(BadWordsFilter.class)) {
            mockedFilter.when(() -> BadWordsFilter.isBadWord(badNickname)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.validateNickname(userId, badNickname))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BAD_WORD_FILTER);
        }
    }

    @Test
    @DisplayName("닉네임 유효성 검증 실패 - 중복된 닉네임")
    void shouldFailValidateNicknameWithDuplicate() {
        // given
        String duplicateNickname = "중복닉네임";
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(redisRepository.getJsonData(anyString(), eq(UserResponseDto.class))).thenReturn(null);
        when(userRepository.existsByNickname(duplicateNickname)).thenReturn(true);

        try (MockedStatic<BadWordsFilter> mockedFilter = mockStatic(BadWordsFilter.class)) {
            mockedFilter.when(() -> BadWordsFilter.isBadWord(duplicateNickname)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.validateNickname(userId, duplicateNickname))
                    .isInstanceOf(ApiException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
        }
    }

    @Test
    @DisplayName("닉네임 업데이트 성공")
    void shouldUpdateUserNickname() {
        // given
        String newNickname = "새닉네임";
        NicknameRequestDto requestDto = new NicknameRequestDto(newNickname);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(redisRepository.getJsonData(anyString(), eq(UserResponseDto.class))).thenReturn(null);
        when(userRepository.existsByNickname(newNickname)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        try (MockedStatic<BadWordsFilter> mockedFilter = mockStatic(BadWordsFilter.class)) {
            mockedFilter.when(() -> BadWordsFilter.isBadWord(newNickname)).thenReturn(false);

            // when
            UserResponseDto result = userService.updateUserNickname(userId, requestDto);

            // then
            assertThat(result.nickname()).isEqualTo(newNickname);
            verify(userRepository).save(any(User.class));
            verify(redisRepository).setJsonDataWithExpire(anyString(), any(UserResponseDto.class), anyInt());
        }
    }

    @Test
    @DisplayName("ID로 사용자 정보 조회 - 캐시에서 조회")
    void shouldGetUserInfoByIdFromCache() {
        // given
        UserResponseDto cachedUser = new UserResponseDto(userId, nickname, true, "https://example.com/profile.jpg");
        when(redisRepository.getJsonData(anyString(), eq(UserResponseDto.class))).thenReturn(cachedUser);

        // when
        UserResponseDto result = userService.getInfoById(userId);

        // then
        assertThat(result).isEqualTo(cachedUser);
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    @DisplayName("ID로 사용자 정보 조회 - DB에서 조회")
    void shouldGetUserInfoByIdFromDb() {
        // given
        when(redisRepository.getJsonData(anyString(), eq(UserResponseDto.class))).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // when
        UserResponseDto result = userService.getInfoById(userId);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.nickname()).isEqualTo(nickname);
        verify(redisRepository).setJsonDataWithExpire(anyString(), any(UserResponseDto.class), anyInt());
    }

    @Test
    @DisplayName("닉네임으로 사용자 정보 조회 성공")
    void shouldGetUserInfoByNickname() {
        // given
        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(testUser));

        // when
        UserResponseDto result = userService.getInfoByNickname(nickname);

        // then
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.nickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("ID 또는 닉네임으로 사용자 정보 조회 - ID로 조회")
    void shouldGetUserInfoByIdOrNickname_WithId() {
        // given
        when(redisRepository.getJsonData(anyString(), eq(UserResponseDto.class))).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // when
        UserResponseDto result = userService.getInfoByIdOrNickname(userId, null);

        // then
        assertThat(result.userId()).isEqualTo(userId);
    }

    @Test
    @DisplayName("ID 또는 닉네임으로 사용자 정보 조회 - 닉네임으로 조회")
    void shouldGetUserInfoByIdOrNickname_WithNickname() {
        // given
        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(testUser));

        // when
        UserResponseDto result = userService.getInfoByIdOrNickname(null, nickname);

        // then
        assertThat(result.nickname()).isEqualTo(nickname);
    }

    @Test
    @DisplayName("ID 또는 닉네임으로 사용자 정보 조회 실패 - 둘 다 제공되거나 둘 다 없는 경우")
    void shouldFailGetUserInfoByIdOrNickname_InvalidParams() {
        // when & then
        assertThatThrownBy(() -> userService.getInfoByIdOrNickname(userId, nickname))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode._BAD_REQUEST);

        assertThatThrownBy(() -> userService.getInfoByIdOrNickname(null, null))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode._BAD_REQUEST);
    }

    @Test
    @DisplayName("사용자 삭제")
    void shouldDeleteUser() {
        // given & when
        userService.deleteUserById(userId);

        // then
        verify(userRepository).deleteById(userId);
        verify(redisRepository).deleteData(anyString());
    }

    @Test
    @DisplayName("온라인 상태 설정")
    void shouldGoOnline() {
        // given
        OnlineRequestDto onlineRequest = new OnlineRequestDto(1640995200.0, 1);

        // when
        userService.goOnline(userId, onlineRequest);

        // then
        verify(redisRepository).setJsonDataWithExpire(anyString(), eq(onlineRequest), eq(86400));
    }

    @Test
    @DisplayName("여러 사용자의 온라인 상태 조회")
    void shouldGetMultipleUsersOnlineDetails() {
        // given
        List<String> userIds = Arrays.asList("user1", "user2", "user3");
        OnlineRequestDto online1 = new OnlineRequestDto(1640995200.0, 1);
        OnlineRequestDto online2 = new OnlineRequestDto(1640995300.0, 1);

        Map<String, OnlineRequestDto> mockData = Map.of(
                "userOnline:user1", online1,
                "userOnline:user2", online2
        );

        when(redisRepository.multiGetJson(anyList(), eq(OnlineRequestDto.class))).thenReturn(mockData);

        // when
        Map<String, OnlineRequestDto> result = userService.getUserOnlineDetails(userIds);

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get("user1")).isEqualTo(online1);
        assertThat(result.get("user2")).isEqualTo(online2);
        assertThat(result.get("user3")).isNull();
    }

    @Test
    @DisplayName("프로필 이미지 업로드 실패 - 파일이 비어있음")
    void shouldFailUploadProfileImageWithEmptyFile() {
        // given
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.uploadProfileImage(userId, emptyFile))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE);
    }

    @Test
    @DisplayName("프로필 이미지 업로드 실패 - 파일 크기 초과")
    void shouldFailUploadProfileImageWithLargeFile() {
        // given
        MultipartFile largeFile = mock(MultipartFile.class);
        when(largeFile.isEmpty()).thenReturn(false);
        when(largeFile.getSize()).thenReturn(10_000_001L);
        when(s3Properties.maxFileSize()).thenReturn(10_000_000L);

        // when & then
        assertThatThrownBy(() -> userService.uploadProfileImage(userId, largeFile))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_SIZE_EXCEEDED);
    }

    @Test
    @DisplayName("프로필 이미지 업로드 실패 - 잘못된 파일 타입")
    void shouldFailUploadProfileImageWithInvalidType() {
        // given
        MultipartFile invalidFile = mock(MultipartFile.class);
        when(invalidFile.isEmpty()).thenReturn(false);
        when(invalidFile.getSize()).thenReturn(1000L);
        when(invalidFile.getContentType()).thenReturn("application/pdf");
        when(s3Properties.maxFileSize()).thenReturn(10_000_000L);
        when(s3Properties.allowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png"));

        // when & then
        assertThatThrownBy(() -> userService.uploadProfileImage(userId, invalidFile))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_TYPE);
    }
}