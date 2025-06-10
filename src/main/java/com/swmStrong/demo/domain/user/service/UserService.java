package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    TokenResponseDto signupGuest(HttpServletRequest request, UserRequestDto userRequestDto);
    boolean isNicknameDuplicated(String nickname);
    UserResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto);
    UserResponseDto getMyInfo(String userId);
}
