package com.swmStrong.demo.domain.loginCredential.dto;

public record UpgradeRequestDto(
        String userId,
        String email,
        String password
) {
}
