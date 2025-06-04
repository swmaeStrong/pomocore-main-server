package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.enums.Role;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserUpdateProviderImpl implements UserUpdateProvider {

    private final UserRepository userRepository;

    public UserUpdateProviderImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void updateUserRole(User user) {
        user.updateRole(Role.USER);
        userRepository.save(user);
    }

    @Override
    public User getUserByUserId(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    }
}
