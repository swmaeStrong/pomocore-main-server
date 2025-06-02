package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.domain.user.entity.User;

public interface UserInfoProvider {
    String getNicknameByUserId(String userId);
}
