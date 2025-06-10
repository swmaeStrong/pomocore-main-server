package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    UserResponseDto signupGuest(HttpServletRequest request, UserRequestDto userRequestDto);
    boolean isNicknameDuplicated(String nickname);
    TokenResponseDto getToken(HttpServletRequest request, UnregisteredRequestDto unregisteredRequestDto);
    UserInfoResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto);
    UserInfoResponseDto getMyInfo(String userId);
}
