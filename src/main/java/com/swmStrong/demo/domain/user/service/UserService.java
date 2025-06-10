package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {
    TokenResponseDto signupGuest(HttpServletRequest request, UserRequestDto userRequestDto);
    boolean isNicknameDuplicated(String nickname);
    UserResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto);
    UserResponseDto getInfoById(String userId);
    UserResponseDto getInfoByNickname(String nickname);
    UserResponseDto getInfoByIdOrNickname(String userId, String nickname);
}
