package com.swmStrong.demo.domain.loginCredential.entity;

import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginCredential 엔티티 테스트")
class LoginCredentialTest {

    @Test
    @DisplayName("Builder로 일반 로그인 계정 생성")
    void shouldCreateRegularLoginCredentialWithBuilder() {
        // given
        User user = new User("user123", "테스트사용자");
        String email = "test@example.com";
        String password = "password123";

        // when
        LoginCredential loginCredential = LoginCredential.builder()
                .user(user)
                .email(email)
                .password(password)
                .build();

        // then
        assertThat(loginCredential.getId()).isEqualTo("user123");
        assertThat(loginCredential.getNickname()).isEqualTo("테스트사용자");
        assertThat(loginCredential.getEmail()).isEqualTo(email);
        assertThat(loginCredential.getPassword()).isEqualTo(password);
        assertThat(loginCredential.isSocial()).isFalse();
        assertThat(loginCredential.getSocialId()).isNull();
        assertThat(loginCredential.getRole()).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("소셜 로그인 계정 생성")
    void shouldCreateSocialLoginCredential() {
        // given
        String email = "social@example.com";
        String socialId = "google123456789";

        // when
        LoginCredential loginCredential = LoginCredential.createSocialLoginCredential(email, socialId);

        // then
        assertThat(loginCredential.getEmail()).isEqualTo(email);
        assertThat(loginCredential.getSocialId()).isEqualTo(socialId);
        assertThat(loginCredential.isSocial()).isTrue();
        assertThat(loginCredential.getPassword()).isNull();
        assertThat(loginCredential.getId()).isNotNull();
        assertThat(loginCredential.getNickname()).isNull();
        assertThat(loginCredential.getRole()).isEqualTo(Role.UNREGISTERED);
    }

    @Test
    @DisplayName("기존 계정에 소셜 계정 연결")
    void shouldConnectSocialAccountToExistingCredential() {
        // given
        User user = new User("user123", "테스트사용자");
        LoginCredential loginCredential = LoginCredential.builder()
                .user(user)
                .email("test@example.com")
                .password("password123")
                .build();
        String socialId = "google123456789";

        // when
        LoginCredential result = loginCredential.connectSocialAccount(socialId);

        // then
        assertThat(result).isSameAs(loginCredential);
        assertThat(loginCredential.getSocialId()).isEqualTo(socialId);
        assertThat(loginCredential.getRole()).isEqualTo(Role.USER);
        assertThat(loginCredential.isSocial()).isFalse(); // 원래 일반 계정은 isSocial이 false로 유지
    }

    @Test
    @DisplayName("일반 로그인 계정과 소셜 로그인 계정의 차이 확인")
    void shouldDifferentiateBetweenRegularAndSocialAccounts() {
        // given
        User regularUser = new User("regular123", "일반사용자");
        LoginCredential regularCredential = LoginCredential.builder()
                .user(regularUser)
                .email("regular@example.com")
                .password("password123")
                .build();

        LoginCredential socialCredential = LoginCredential.createSocialLoginCredential(
                "social@example.com",
                "google123456789"
        );

        // then
        assertThat(regularCredential.isSocial()).isFalse();
        assertThat(regularCredential.getPassword()).isNotNull();
        assertThat(regularCredential.getSocialId()).isNull();
        assertThat(regularCredential.getRole()).isEqualTo(Role.USER);

        assertThat(socialCredential.isSocial()).isTrue();
        assertThat(socialCredential.getPassword()).isNull();
        assertThat(socialCredential.getSocialId()).isNotNull();
        assertThat(socialCredential.getRole()).isEqualTo(Role.UNREGISTERED);
    }

    @Test
    @DisplayName("User 정보 상속 확인")
    void shouldInheritUserInformation() {
        // given
        User user = new User("user123", "테스트사용자");
        LoginCredential loginCredential = LoginCredential.builder()
                .user(user)
                .email("test@example.com")
                .password("password123")
                .build();

        // when & then
        assertThat(loginCredential).isInstanceOf(User.class);
        assertThat(loginCredential.getId()).isEqualTo(user.getId());
        assertThat(loginCredential.getNickname()).isEqualTo(user.getNickname());

        // User의 메서드들도 사용 가능한지 확인
        loginCredential.updateNickname("새닉네임");
        assertThat(loginCredential.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("소셜 계정 연결 후 Role 변경 확인")
    void shouldUpdateRoleAfterConnectingSocialAccount() {
        // given
        User user = new User("user123", "테스트사용자");
        user.updateRole(Role.UNREGISTERED); // 초기에 UNREGISTERED로 설정

        LoginCredential loginCredential = LoginCredential.builder()
                .user(user)
                .email("test@example.com")
                .password("password123")
                .build();

        // 빌더에서 Role.USER로 설정되므로 다시 UNREGISTERED로 변경
        loginCredential.updateRole(Role.UNREGISTERED);
        String socialId = "google123456789";

        // when
        loginCredential.connectSocialAccount(socialId);

        // then
        assertThat(loginCredential.getRole()).isEqualTo(Role.USER);
        assertThat(loginCredential.getSocialId()).isEqualTo(socialId);
    }
}