package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.domain.user.dto.OnlineRequestDto;
import com.swmStrong.demo.domain.user.entity.User;

import java.util.List;
import java.util.Map;

public interface UserInfoProvider {
    String loadNicknameByUserId(String userId);
    Map<String, String> loadNicknamesByUserIds(List<String> userIds);
    boolean existsUserById(String userId);
    User loadByUserId(String userId);
    Map<String, OnlineRequestDto> getUserOnlineDetails(List<String> userIds);
}
