package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.util.redis.RedisUtil;
import com.swmStrong.demo.util.token.TokenUtil;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.swmStrong.demo.util.redis.RedisUtil.REGISTER_IP_COUNT_PREFIX;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TokenUtil tokenUtil;
    private final RedisUtil redisUtil;

    public UserServiceImpl(
            UserRepository userRepository,
            TokenUtil tokenUtil,
            RedisUtil redisUtil
    ) {
        this.userRepository = userRepository;
        this.tokenUtil = tokenUtil;
        this.redisUtil = redisUtil;
    }

    @Override
    public TokenResponseDto signupGuest(HttpServletRequest request, UserRequestDto userRequestDto) {
        if (userRepository.existsById(userRequestDto.userId())) {
            throw new ApiException(ErrorCode.DUPLICATE_USER_ID);
        }

        String requestIP = request.getHeader("X-Forwarded-For");
        if (requestIP == null) {
            requestIP = request.getRemoteAddr();
        }

        Long count = redisUtil.incrementWithExpireIfFirst(getKey(requestIP), 1, TimeUnit.HOURS);
        if (count > 5) {
            throw new ApiException(ErrorCode.IP_RATE_LIMIT_EXCEEDED);
        }

        User user = userRepository.save(User.of(userRequestDto));
        return tokenUtil.getToken(user.getId(), request.getHeader("User-Agent"), Role.UNREGISTERED);
    }

    @Override
    public boolean isNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    public UserResponseDto updateUserNickname(String userId, NicknameRequestDto nicknameRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (isNicknameDuplicated(nicknameRequestDto.nickname())) {
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }

        user.updateNickname(nicknameRequestDto.nickname());
        userRepository.save(user);
        return UserResponseDto.of(user);
    }

    @Override
    public UserResponseDto getMyInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return UserResponseDto.of(user);
    }

    private String getKey(String requestIP) {
        return REGISTER_IP_COUNT_PREFIX+requestIP;
    }
}
