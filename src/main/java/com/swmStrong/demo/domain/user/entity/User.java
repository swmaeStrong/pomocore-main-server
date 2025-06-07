package com.swmStrong.demo.domain.user.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.user.dto.UserRequestDto;
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

    public static User of(UserRequestDto userRequestDto) {
        return new User(
                userRequestDto.userId(),
                userRequestDto.nickname()
        );
    }

    public User(String id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public void updateRole(Role role) {
        this.role = role;
    }
}
