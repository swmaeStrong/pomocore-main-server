package com.swmStrong.demo.domain.user.dto;

import jakarta.validation.constraints.Pattern;

public record UserRequestDto(
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
                message = "올바른 UUID 포맷이 아닙니다."
        )
        String userId
) {
    public static UserRequestDto of(String userId) {
        return new UserRequestDto(userId);
    }
}
