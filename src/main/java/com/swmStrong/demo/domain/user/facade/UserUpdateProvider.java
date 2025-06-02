package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.domain.user.entity.User;

public interface UserUpdateProvider {
    void updateUserRole(User user);
    User getUserByUserId(String userId);
}
