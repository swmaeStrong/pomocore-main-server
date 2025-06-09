package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.util.token.TokenUtil;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TokenUtil tokenUtil;

    public UserServiceImpl(UserRepository userRepository, TokenUtil tokenUtil) {
        this.userRepository = userRepository;
        this.tokenUtil = tokenUtil;
    }

    @Override
    public UserResponseDto signupGuest(UserRequestDto userRequestDto) {
        if (userRepository.existsById(userRequestDto.userId())){
            throw new ApiException(ErrorCode.DUPLICATE_USER_ID);
        }

        User user = userRepository.save(User.of(userRequestDto));

        return UserResponseDto.of(user);
    }

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    public TokenResponseDto getToken(HttpServletRequest request, UnregisteredRequestDto unregisteredRequestDto) {
        User user = userRepository.findById(unregisteredRequestDto.userId())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (!user.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)
                .equals(unregisteredRequestDto.createdAt().truncatedTo(ChronoUnit.MILLIS))
        ) {
            throw new ApiException(ErrorCode.USER_MISMATCH);
        }

        return tokenUtil.getToken(unregisteredRequestDto.userId(),  request.getHeader("User-Agent"), Role.UNREGISTERED);
    }

    @Override
    public UserInfoResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (isNicknameDuplicated(nicknameRequestDto.nickname())) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateNickname(nicknameRequestDto.nickname());
        userRepository.save(user);
        return UserInfoResponseDto.of(user);
    }

    @Override
    public UserInfoResponseDto getMyInfo(SecurityPrincipal securityPrincipal) {
        return UserInfoResponseDto.of(securityPrincipal);
    }
}
