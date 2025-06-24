package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.domain.user.entity.User;

public interface UserUpdateProvider {
    User updateUserRole(User user);
    User getUserByUserId(String userId);
}
