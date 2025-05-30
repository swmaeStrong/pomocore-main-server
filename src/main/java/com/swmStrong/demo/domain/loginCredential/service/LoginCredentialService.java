package com.swmStrong.demo.domain.loginCredential.service;

import com.swmStrong.demo.domain.loginCredential.dto.UpgradeRequestDto;
import com.swmStrong.demo.util.token.dto.RefreshTokenRequestDto;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginCredentialService {
    void upgradeToUser(UpgradeRequestDto upgradeRequestDto);
    TokenResponseDto tokenRefresh(String userId, HttpServletRequest request, RefreshTokenRequestDto refreshTokenRequestDto);
}
