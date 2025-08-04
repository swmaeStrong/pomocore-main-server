package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserService {
    TokenResponseDto signupGuest(HttpServletRequest request, UserRequestDto userRequestDto);
    void validateNickname(String userId, String nickname);
    UserResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto);
    UserResponseDto getInfoById(String userId);
    UserResponseDto getInfoByNickname(String nickname);
    UserResponseDto getInfoByIdOrNickname(String userId, String nickname);
    void deleteUserById(String userId);
    String uploadProfileImage(String userId, MultipartFile file);
    void deleteProfileImage(String userId);
    void goOnline(String userId, OnlineRequestDto onlineRequestDto);
    Map<String, Double> getUserOnline(List<String> userIds);
    void dropOut(String userId);
}
