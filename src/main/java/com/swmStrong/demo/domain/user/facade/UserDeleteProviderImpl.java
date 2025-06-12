package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserDeleteProviderImpl implements UserDeleteProvider {

    private final UserRepository userRepository;

    public UserDeleteProviderImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void deleteById(String userId) {
        userRepository.deleteById(userId);
    }
}
