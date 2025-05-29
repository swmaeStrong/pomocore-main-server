package com.swmStrong.demo.domain.user.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.global.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
public class User extends BaseEntity {
    @Id
    protected String id;
    protected String nickname;

    @Enumerated(EnumType.STRING)
    private Role role = Role.UNREGISTERED;


    public User(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public User(String id, String nickname, Role role) {
        this.id = id;
        this.nickname = nickname;
        this.role = role;
    }
}
