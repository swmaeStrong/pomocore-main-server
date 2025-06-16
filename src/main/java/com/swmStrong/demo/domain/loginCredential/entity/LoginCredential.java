package com.swmStrong.demo.domain.loginCredential.entity;

import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.user.dto.UserRequestDto;
import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@PrimaryKeyJoinColumn(name = "id")
public class LoginCredential extends User {

    private String email;

    private String password;

    // 소셜로 시작한 경우 true로, 이 때 일반 로그인이 아예 되지 않도록 막기
    @Column(name = "is_social", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private boolean isSocial;

    private String socialId;

    private void setUser(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
    }

    @Builder
    public LoginCredential(User user, String email, String password) {
        setUser(user);
        this.email = email;
        this.password = password;
        this.isSocial = false;
        this.updateRole(Role.USER);
    }

    private void setSocial() {
        this.isSocial = true;
    }

    private void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public LoginCredential connectSocialAccount(String socialId) {
        this.setSocialId(socialId);
        this.updateRole(Role.USER);
        return this;
    }

    public static LoginCredential createSocialLoginCredential(String email, String socialId) {
        LoginCredential loginCredential = LoginCredential.builder()
                .user(User.of(UserRequestDto.of(UUID.randomUUID().toString())))
                .email(email)
                .build();
        loginCredential.setSocial();
        loginCredential.setSocialId(socialId);
        return loginCredential;
    }
}
