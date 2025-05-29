package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

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
}
