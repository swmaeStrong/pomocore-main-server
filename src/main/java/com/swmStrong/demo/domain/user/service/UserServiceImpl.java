package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public Void registerGuestNickname(String deviceId, String nickname) {
        User user = new User();
        if (userRepository.existsByNickname(nickname)){
            throw new ApiException(ErrorCode.DUPLICATE_NICKNAME);
        }
        if (userRepository.existsByDeviceId(deviceId)){
            throw new ApiException(ErrorCode.DUPLICATE_DEVICE_ID);
        }

        user.setDeviceId(deviceId);
        user.setNickname(nickname);
        userRepository.save(user);
        return null;
    }

    public boolean isGuestRegistered(String deviceId) {
        return userRepository.existsByDeviceId(deviceId);
    }

    public boolean isGuestNicknameDuplicated(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

}
