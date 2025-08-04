package com.swmStrong.demo.domain.user.dto;

public record OnlineRequestDto(
        double timestamp,
        int sessionMinutes
) {
}
