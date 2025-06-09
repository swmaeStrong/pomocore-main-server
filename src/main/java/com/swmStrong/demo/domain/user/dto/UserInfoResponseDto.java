package com.swmStrong.demo.domain.user.dto;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.user.entity.User;

public record UserInfoResponseDto(
        String userId,
        String nickname
) {
    public static UserInfoResponseDto of(User user) {
        return new UserInfoResponseDto(user.getId(), user.getNickname());
    }

    public static UserInfoResponseDto of(SecurityPrincipal securityPrincipal) {
        return new UserInfoResponseDto(securityPrincipal.userId(), securityPrincipal.nickname());
    }
}
