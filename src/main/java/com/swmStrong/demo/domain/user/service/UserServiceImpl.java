package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.global.Role;
import com.swmStrong.demo.domain.user.dto.UnregisteredRequestDto;
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
    public void registerGuestNickname(String userId, String nickname) {

        if (userRepository.existsByNickname(nickname)){
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (userRepository.existsById(userId)){
            throw new ApiException(ErrorCode.DUPLICATE_DEVICE_ID);
        }

        userRepository.save(new User(userId, nickname));
    }

    @Override
    public boolean isGuestNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    @Override
    public TokenResponseDto getToken(HttpServletRequest request, UnregisteredRequestDto unregisteredRequestDto) {
        User user = userRepository.findById(unregisteredRequestDto.userId())
                .orElseThrow(IllegalArgumentException::new);

        if (!user.getCreatedAt().truncatedTo(ChronoUnit.MILLIS)
                .equals(unregisteredRequestDto.createdAt().truncatedTo(ChronoUnit.MILLIS))
        ) {
            throw new IllegalArgumentException("비정상적인 요청 입니다..");
        }

        return tokenUtil.getToken(unregisteredRequestDto.userId(),  request.getHeader("User-Agent"), Role.UNREGISTERED);
    }
}
