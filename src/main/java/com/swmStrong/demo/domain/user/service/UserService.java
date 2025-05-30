package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.domain.user.dto.UnregisteredRequestDto;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    void registerGuestNickname(String userId, String nickname);
    boolean isGuestNicknameDuplicated(String nickname);
    TokenResponseDto getToken(HttpServletRequest request, UnregisteredRequestDto unregisteredRequestDto);
}
