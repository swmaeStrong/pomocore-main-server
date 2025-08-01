package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    TokenResponseDto signupGuest(HttpServletRequest request, UserRequestDto userRequestDto);
    void validateNickname(String nickname);
    UserResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto);
    UserResponseDto getInfoById(String userId);
    UserResponseDto getInfoByNickname(String nickname);
    UserResponseDto getInfoByIdOrNickname(String userId, String nickname);
    void deleteUserById(String userId);
    String uploadProfileImage(String userId, MultipartFile file);
    void deleteProfileImage(String userId);
}
