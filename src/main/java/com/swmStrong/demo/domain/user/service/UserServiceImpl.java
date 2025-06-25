package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.user.dto.*;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import com.swmStrong.demo.infra.token.TokenManager;
import com.swmStrong.demo.infra.token.dto.TokenResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final RedisRepository redisRepository;

    public static final String REGISTER_IP_COUNT_PREFIX = "registerIpCount:";

    public UserServiceImpl(
            UserRepository userRepository,
            TokenManager tokenManager,
            RedisRepository redisRepository
    ) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
        this.redisRepository = redisRepository;
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

        Long count = redisRepository.incrementWithExpireIfFirst(getKey(requestIP), 1, TimeUnit.HOURS);
        if (count > 5) {
            throw new ApiException(ErrorCode.IP_RATE_LIMIT_EXCEEDED);
        }

        User user = userRepository.save(User.of(userRequestDto));
        return tokenManager.getToken(user.getId(), request.getHeader("User-Agent"), Role.UNREGISTERED);
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
    public UserResponseDto getInfoByIdOrNickname(String userId, String nickname) {
        boolean isUserIdExists = userId != null && !userId.isEmpty();
        boolean isNicknameExists = nickname != null && !nickname.isEmpty();
        if (isUserIdExists ^ isNicknameExists) {
            if (isUserIdExists) {
                return getInfoById(userId);
            } else {
                return getInfoByNickname(nickname);
            }
        }
        throw new ApiException(ErrorCode._BAD_REQUEST);
    }

    @Override
    public UserResponseDto getInfoById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        return UserResponseDto.of(user);
    }

    @Override
    public UserResponseDto getInfoByNickname(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new ApiException(ErrorCode.NICKNAME_NOT_FOUND));

        return UserResponseDto.of(user);
    }

    @Override
    public void deleteUserById(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        userRepository.deleteById(userId);
    }

    private String getKey(String requestIP) {
        return REGISTER_IP_COUNT_PREFIX+requestIP;
    }
}
