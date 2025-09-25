package com.swmStrong.demo.domain.user.entity;

import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.user.dto.UserRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User 엔티티 테스트")
class UserTest {

    @Test
    @DisplayName("UserRequestDto로부터 User 생성")
    void shouldCreateUserFromUserRequestDto() {
        // given
        String userId = "test123";
        UserRequestDto userRequestDto = new UserRequestDto(userId);

        // when
        User user = User.of(userRequestDto);

        // then
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getNickname()).isNull();
        assertThat(user.getRole()).isEqualTo(Role.UNREGISTERED);
        assertThat(user.isOnBoarded()).isFalse();
    }

    @Test
    @DisplayName("생성자로 User 생성")
    void shouldCreateUserWithConstructor() {
        // given
        String userId = "test123";
        String nickname = "테스트닉네임";

        // when
        User user = new User(userId, nickname);

        // then
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getRole()).isEqualTo(Role.UNREGISTERED);
        assertThat(user.isOnBoarded()).isFalse();
    }

    @Test
    @DisplayName("역할 업데이트")
    void shouldUpdateRole() {
        // given
        User user = new User("test123", "테스트닉네임");
        Role newRole = Role.USER;

        // when
        user.updateRole(newRole);

        // then
        assertThat(user.getRole()).isEqualTo(newRole);
    }

    @Test
    @DisplayName("닉네임 업데이트")
    void shouldUpdateNickname() {
        // given
        User user = new User("test123", "기존닉네임");
        String newNickname = "새닉네임";

        // when
        user.updateNickname(newNickname);

        // then
        assertThat(user.getNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("프로필 이미지 업데이트")
    void shouldUpdateProfileImage() {
        // given
        User user = new User("test123", "테스트닉네임");
        String profileImageUrl = "https://example.com/profile.jpg";
        String profileImageKey = "profile-key-123";

        // when
        user.updateProfileImage(profileImageUrl, profileImageKey);

        // then
        assertThat(user.getProfileImageUrl()).isEqualTo(profileImageUrl);
        assertThat(user.getProfileImageKey()).isEqualTo(profileImageKey);
    }

    @Test
    @DisplayName("온보딩 완료")
    void shouldCompleteOnBoard() {
        // given
        User user = new User("test123", "테스트닉네임");
        assertThat(user.isOnBoarded()).isFalse();

        // when
        user.completeOnBoard();

        // then
        assertThat(user.isOnBoarded()).isTrue();
    }
}