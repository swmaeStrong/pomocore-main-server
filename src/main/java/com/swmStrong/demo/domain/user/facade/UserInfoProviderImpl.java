package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserInfoProviderImpl implements UserInfoProvider {

    private final UserRepository userRepository;

    public UserInfoProviderImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String getNicknameByUserId(String userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse("Unknown");
    }

    @Override
    public User getUserByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(IllegalArgumentException::new);
    }
}
