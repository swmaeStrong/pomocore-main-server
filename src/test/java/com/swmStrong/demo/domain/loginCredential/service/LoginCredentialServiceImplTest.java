package com.swmStrong.demo.domain.loginCredential.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.loginCredential.dto.SocialLoginRequestDto;
import com.swmStrong.demo.domain.loginCredential.dto.SocialLoginResult;
import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.domain.loginCredential.entity.LoginCredential;
import com.swmStrong.demo.domain.loginCredential.repository.LoginCredentialRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserUpdateProvider;
import com.swmStrong.demo.infra.mail.MailSender;
import com.swmStrong.demo.infra.token.TokenManager;
import com.swmStrong.demo.infra.token.dto.RefreshTokenRequestDto;
import com.swmStrong.demo.infra.token.dto.SubjectResponseDto;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginCredentialServiceImpl 테스트")
class LoginCredentialServiceImplTest {

    @Mock
    private LoginCredentialRepository loginCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserUpdateProvider userUpdateProvider;

    @Mock
    private TokenManager tokenManager;

    @Mock
    private MailSender mailSender;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private LoginCredentialServiceImpl loginCredentialService;

    private User testUser;
    private LoginCredential testLoginCredential;
    private String userId;
    private String email;
    private String password;
    private String encodedPassword;

    @BeforeEach
    void setUp() {
        userId = "user123";
        email = "test@example.com";
        password = "testPassword123!";
        encodedPassword = "encodedPassword";

        testUser = new User(userId, "테스트사용자");
        testUser.updateRole(Role.UNREGISTERED);

        testLoginCredential = LoginCredential.builder()
                .user(testUser)
                .email(email)
                .password(encodedPassword)
                .build();
    }

    @Test
    @DisplayName("게스트 사용자를 일반 사용자로 업그레이드")
    void shouldUpgradeToUser() {
        // given
        UpgradeRequestDto upgradeRequestDto = new UpgradeRequestDto(email, password);
        TokenResponseDto expectedToken = new TokenResponseDto("accessToken", "refreshToken");

        when(loginCredentialRepository.existsByEmail(email)).thenReturn(false);
        when(userUpdateProvider.getUserByUserId(userId)).thenReturn(testUser);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userUpdateProvider.updateUserRole(testUser)).thenReturn(testLoginCredential);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        when(tokenManager.getToken(userId, "TestAgent", Role.USER)).thenReturn(expectedToken);

        // when
        TokenResponseDto result = loginCredentialService.upgradeToUser(httpServletRequest, userId, upgradeRequestDto);

        // then
        assertThat(result).isEqualTo(expectedToken);
        verify(loginCredentialRepository).insertLoginCredential(userId, email, encodedPassword);
        verify(userUpdateProvider).updateUserRole(testUser);
        verify(mailSender).sendWelcomeEmail(email, "사용자");
    }

    @Test
    @DisplayName("업그레이드 실패 - 이메일 중복")
    void shouldFailUpgradeWithDuplicateEmail() {
        // given
        UpgradeRequestDto upgradeRequestDto = new UpgradeRequestDto(email, password);
        when(loginCredentialRepository.existsByEmail(email)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> loginCredentialService.upgradeToUser(httpServletRequest, userId, upgradeRequestDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USER_EMAIL);
    }

    @Test
    @DisplayName("업그레이드 실패 - 이미 등록된 사용자")
    void shouldFailUpgradeAlreadyRegisteredUser() {
        // given
        UpgradeRequestDto upgradeRequestDto = new UpgradeRequestDto(email, password);
        when(loginCredentialRepository.existsByEmail(email)).thenReturn(false);
        when(userUpdateProvider.getUserByUserId(userId)).thenReturn(testLoginCredential);

        // when & then
        assertThatThrownBy(() -> loginCredentialService.upgradeToUser(httpServletRequest, userId, upgradeRequestDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_REGISTERED);
    }

    @Test
    @DisplayName("토큰 리프레시")
    void shouldRefreshToken() {
        // given
        RefreshTokenRequestDto refreshTokenRequestDto = new RefreshTokenRequestDto(userId,"refreshToken");
        TokenResponseDto expectedToken = new TokenResponseDto("newAccessToken", "newRefreshToken");

        when(httpServletRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        when(tokenManager.tokenRefresh(refreshTokenRequestDto, "TestAgent")).thenReturn(expectedToken);

        // when
        TokenResponseDto result = loginCredentialService.tokenRefresh(httpServletRequest, refreshTokenRequestDto);

        // then
        assertThat(result).isEqualTo(expectedToken);
    }

    @Test
    @DisplayName("소셜 로그인 - 새 사용자")
    void shouldSocialLoginNewUser() {
        // given
        String accessToken = "validAccessToken";
        String supabaseId = "supabase123";
        SocialLoginRequestDto socialLoginRequestDto = new SocialLoginRequestDto(accessToken);
        SubjectResponseDto subjectResponseDto = new SubjectResponseDto(email, supabaseId);
        TokenResponseDto expectedToken = new TokenResponseDto("accessToken", "refreshToken");

        when(tokenManager.isTokenValid(accessToken)).thenReturn(true);
        when(tokenManager.loadSubjectByToken(accessToken)).thenReturn(subjectResponseDto);
        when(loginCredentialRepository.findBySocialId(supabaseId)).thenReturn(Optional.empty());
        when(loginCredentialRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        when(tokenManager.getToken(any(), eq("TestAgent"), eq(Role.USER))).thenReturn(expectedToken);

        // when
        SocialLoginResult result = loginCredentialService.socialLogin(httpServletRequest, socialLoginRequestDto);

        // then
        assertThat(result.isNewUser()).isTrue();
        assertThat(result.tokenResponseDto()).isEqualTo(expectedToken);
        verify(loginCredentialRepository).save(any(LoginCredential.class));
        verify(mailSender).sendWelcomeEmail(email, "사용자");
    }

    @Test
    @DisplayName("소셜 로그인 - 기존 사용자 (소셜 ID로 조회)")
    void shouldSocialLoginExistingUserBySocialId() {
        // given
        String accessToken = "validAccessToken";
        String supabaseId = "supabase123";
        SocialLoginRequestDto socialLoginRequestDto = new SocialLoginRequestDto(accessToken);
        SubjectResponseDto subjectResponseDto = new SubjectResponseDto(email, supabaseId);
        TokenResponseDto expectedToken = new TokenResponseDto("accessToken", "refreshToken");

        when(tokenManager.isTokenValid(accessToken)).thenReturn(true);
        when(tokenManager.loadSubjectByToken(accessToken)).thenReturn(subjectResponseDto);
        when(loginCredentialRepository.findBySocialId(supabaseId)).thenReturn(Optional.of(testLoginCredential));
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        when(tokenManager.getToken(testLoginCredential.getId(), "TestAgent", testLoginCredential.getRole()))
                .thenReturn(expectedToken);

        // when
        SocialLoginResult result = loginCredentialService.socialLogin(httpServletRequest, socialLoginRequestDto);

        // then
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.tokenResponseDto()).isEqualTo(expectedToken);
        verify(loginCredentialRepository).save(testLoginCredential);
    }

    @Test
    @DisplayName("소셜 로그인 - 기존 이메일 사용자와 소셜 계정 연결")
    void shouldSocialLoginConnectExistingEmailUser() {
        // given
        String accessToken = "validAccessToken";
        String supabaseId = "supabase123";
        SocialLoginRequestDto socialLoginRequestDto = new SocialLoginRequestDto(accessToken);
        SubjectResponseDto subjectResponseDto = new SubjectResponseDto(email, supabaseId);
        TokenResponseDto expectedToken = new TokenResponseDto("accessToken", "refreshToken");

        when(tokenManager.isTokenValid(accessToken)).thenReturn(true);
        when(tokenManager.loadSubjectByToken(accessToken)).thenReturn(subjectResponseDto);
        when(loginCredentialRepository.findBySocialId(supabaseId)).thenReturn(Optional.empty());
        when(loginCredentialRepository.findByEmail(email)).thenReturn(Optional.of(testLoginCredential));
        when(testLoginCredential.connectSocialAccount(supabaseId)).thenReturn(testLoginCredential);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        when(tokenManager.getToken(testLoginCredential.getId(), "TestAgent", testLoginCredential.getRole()))
                .thenReturn(expectedToken);

        // when
        SocialLoginResult result = loginCredentialService.socialLogin(httpServletRequest, socialLoginRequestDto);

        // then
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.tokenResponseDto()).isEqualTo(expectedToken);
        verify(loginCredentialRepository).save(testLoginCredential);
    }

    @Test
    @DisplayName("소셜 로그인 실패 - 유효하지 않은 토큰")
    void shouldFailSocialLoginWithInvalidToken() {
        // given
        String invalidToken = "invalidToken";
        SocialLoginRequestDto socialLoginRequestDto = new SocialLoginRequestDto(invalidToken);
        when(tokenManager.isTokenValid(invalidToken)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> loginCredentialService.socialLogin(httpServletRequest, socialLoginRequestDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode._INVALID_TOKEN);
    }

    @Test
    @DisplayName("게스트 사용자 소셜 로그인으로 업그레이드")
    void shouldUpgradeGuestBySocialLogin() {
        // given
        String accessToken = "validAccessToken";
        String supabaseId = "supabase123";
        SocialLoginRequestDto socialLoginRequestDto = new SocialLoginRequestDto(accessToken);
        SubjectResponseDto subjectResponseDto = new SubjectResponseDto(email, supabaseId);
        TokenResponseDto expectedToken = new TokenResponseDto("accessToken", "refreshToken");

        when(tokenManager.isTokenValid(accessToken)).thenReturn(true);
        when(userUpdateProvider.getUserByUserId(userId)).thenReturn(testUser);
        when(tokenManager.loadSubjectByToken(accessToken)).thenReturn(subjectResponseDto);
        when(loginCredentialRepository.existsByEmail(email)).thenReturn(false);
        when(userUpdateProvider.updateUserRole(testUser)).thenReturn(testLoginCredential);
        when(httpServletRequest.getHeader("User-Agent")).thenReturn("TestAgent");
        when(tokenManager.getToken(testUser.getId(), "TestAgent", testLoginCredential.getRole()))
                .thenReturn(expectedToken);

        // when
        TokenResponseDto result = loginCredentialService.upgradeGuestBySocialLogin(
                httpServletRequest, userId, socialLoginRequestDto
        );

        // then
        assertThat(result).isEqualTo(expectedToken);
        verify(loginCredentialRepository).insertLoginCredentialWhenSocialLogin(userId, email, supabaseId);
        verify(userUpdateProvider).updateUserRole(testUser);
        verify(mailSender).sendWelcomeEmail(email, "사용자");
    }

    @Test
    @DisplayName("게스트 소셜 로그인 업그레이드 실패 - 이미 등록된 사용자")
    void shouldFailUpgradeGuestSocialLoginAlreadyRegistered() {
        // given
        String accessToken = "validAccessToken";
        SocialLoginRequestDto socialLoginRequestDto = new SocialLoginRequestDto(accessToken);

        when(tokenManager.isTokenValid(accessToken)).thenReturn(true);
        when(userUpdateProvider.getUserByUserId(userId)).thenReturn(testLoginCredential);

        // when & then
        assertThatThrownBy(() -> loginCredentialService.upgradeGuestBySocialLogin(
                httpServletRequest, userId, socialLoginRequestDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_REGISTERED);
    }

    @Test
    @DisplayName("게스트 소셜 로그인 업그레이드 실패 - 이미 소셜 로그인으로 등록된 이메일")
    void shouldFailUpgradeGuestSocialLoginEmailAlreadyExists() {
        // given
        String accessToken = "validAccessToken";
        String supabaseId = "supabase123";
        SocialLoginRequestDto socialLoginRequestDto = new SocialLoginRequestDto(accessToken);
        SubjectResponseDto subjectResponseDto = new SubjectResponseDto(email, supabaseId);

        when(tokenManager.isTokenValid(accessToken)).thenReturn(true);
        when(userUpdateProvider.getUserByUserId(userId)).thenReturn(testUser);
        when(tokenManager.loadSubjectByToken(accessToken)).thenReturn(subjectResponseDto);
        when(loginCredentialRepository.existsByEmail(email)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> loginCredentialService.upgradeGuestBySocialLogin(
                httpServletRequest, userId, socialLoginRequestDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_REGISTERED_BY_SOCIAL_LOGIN);
    }

    @Test
    @DisplayName("게스트 소셜 로그인 업그레이드 실패 - 유효하지 않은 토큰")
    void shouldFailUpgradeGuestSocialLoginInvalidToken() {
        // given
        String invalidToken = "invalidToken";
        SocialLoginRequestDto socialLoginRequestDto = new SocialLoginRequestDto(invalidToken);

        when(tokenManager.isTokenValid(invalidToken)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> loginCredentialService.upgradeGuestBySocialLogin(
                httpServletRequest, userId, socialLoginRequestDto))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode._INVALID_TOKEN);
    }
}