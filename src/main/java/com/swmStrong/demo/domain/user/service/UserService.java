package com.swmStrong.demo.domain.user.service;

import com.swmStrong.demo.domain.user.entity.User;
import org.springframework.stereotype.Service;

public interface UserService {
    public Void registerGuestNickname(String deviceId, String nickname);
    public boolean isGuestRegistered(String deviceId);
    public boolean isGuestNicknameDuplicated(String nickname);
}
