package com.swmStrong.demo.domain.user.dto;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.user.entity.User;

import java.time.LocalDateTime;

public record UserResponseDto(
        String userId,
        String nickname,
        LocalDateTime createdAt
) {
    public static UserResponseDto of(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getNickname(),
                user.getCreatedAt()
        );
    }
}
