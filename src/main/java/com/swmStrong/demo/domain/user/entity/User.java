package com.swmStrong.demo.domain.user.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
public class User extends BaseEntity {
    @Id
    private String id;
    private String deviceId;
    private String nickname;
}
