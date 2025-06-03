package com.swmStrong.demo.domain.user.facade;

import java.util.List;
import java.util.Map;

public interface UserInfoProvider {
    String loadNicknameByUserId(String userId);
    Map<String, String> loadNicknamesByUserIds(List<String> userIds);
}
