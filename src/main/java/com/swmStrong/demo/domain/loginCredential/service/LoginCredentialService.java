package com.swmStrong.demo.domain.loginCredential.service;

import com.swmStrong.demo.domain.loginCredential.dto.SocialLoginRequestDto;
import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.infra.token.dto.RefreshTokenRequestDto;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginCredentialService {
    TokenResponseDto upgradeToUser(HttpServletRequest request, UpgradeRequestDto upgradeRequestDto);
    TokenResponseDto tokenRefresh(HttpServletRequest request, RefreshTokenRequestDto refreshTokenRequestDto);
    TokenResponseDto socialLogin(HttpServletRequest request, SocialLoginRequestDto socialLoginRequestDto);
    TokenResponseDto upgradeGuestBySocialLogin(HttpServletRequest request, String userId, SocialLoginRequestDto socialLoginRequestDto);
}
