package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserDeleteProvider{

    private final UserRepository userRepository;

    public UserDeleteProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public void deleteById(String userId) {
        userRepository.deleteById(userId);
    }
}
