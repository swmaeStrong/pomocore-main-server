package com.swmStrong.demo.domain.loginCredential.entity;

import com.swmStrong.demo.domain.global.Role;
import com.swmStrong.demo.domain.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@PrimaryKeyJoinColumn(name = "id")
public class LoginCredential extends User {

    private String email;

    private String password;

    @Builder
    public LoginCredential(User user, String email, String password) {
        super(user.getId(), user.getNickname(), Role.USER);
        this.email = email;
        this.password = password;
    }
}
