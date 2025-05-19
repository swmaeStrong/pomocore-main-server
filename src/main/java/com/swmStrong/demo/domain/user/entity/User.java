package com.swmStrong.demo.domain.user.entity;

import com.swmStrong.demo.domain.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Setter
@Getter
public class User extends BaseEntity {
    @Id
    private String id = UUID.randomUUID().toString();
    private String deviceId;
    private String nickname;
}
