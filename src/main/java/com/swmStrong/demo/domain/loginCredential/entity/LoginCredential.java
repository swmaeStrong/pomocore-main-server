package com.swmStrong.demo.domain.loginCredential.entity;

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

    private void setUser(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
    }

    @Builder
    public LoginCredential(User user, String email, String password) {
        setUser(user);
        this.email = email;
        this.password = password;
    }
}
