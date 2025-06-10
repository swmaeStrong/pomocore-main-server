package com.swmStrong.demo.domain.user.dto;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.user.entity.User;

public record UserResponseDto(
        String userId,
        String nickname
) {
    public static UserResponseDto of(User user) {
        return new UserResponseDto(user.getId(), user.getNickname());
    }

    public static UserResponseDto of(SecurityPrincipal securityPrincipal) {
        return new UserResponseDto(securityPrincipal.userId(), securityPrincipal.nickname());
    }
}
