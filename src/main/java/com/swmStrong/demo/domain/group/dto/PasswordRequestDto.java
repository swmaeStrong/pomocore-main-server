package com.swmStrong.demo.domain.group.dto;

import lombok.Builder;

@Builder
public record PasswordRequestDto(
        String password
) {
}
