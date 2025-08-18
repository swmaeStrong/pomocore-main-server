package com.swmStrong.demo.domain.loginCredential.dto;

import com.swmStrong.demo.infra.token.dto.TokenResponseDto;

public record SocialLoginResult(
        Boolean isNewUser,
        TokenResponseDto tokenResponseDto
) {
}
