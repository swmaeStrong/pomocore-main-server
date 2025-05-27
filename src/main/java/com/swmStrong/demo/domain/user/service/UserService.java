package com.swmStrong.demo.domain.user.service;

public interface UserService {
    void registerGuestNickname(String userId, String nickname);
    boolean isGuestRegistered(String userId);
    boolean isGuestNicknameDuplicated(String nickname);
}
