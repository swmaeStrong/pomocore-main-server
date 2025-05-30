package com.swmStrong.demo.domain.user.dto;

import java.time.LocalDateTime;

public record UnregisteredRequestDto(
        String userId,
        LocalDateTime createdAt
) {
}
