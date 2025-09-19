package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.domain.user.dto.OnlineRequestDto;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import com.swmStrong.demo.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoProvider {

    private final UserRepository userRepository;
    private final UserService userService;

    public UserInfoProvider(UserRepository userRepository,  UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public String loadNicknameByUserId(String userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse("Unknown");
    }

    public Map<String, String> loadNicknamesByUserIds(List<String> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        Map<String, String> nicknames = new HashMap<>();
        for (User user : users) {
            nicknames.put(user.getId(), user.getNickname());
        }
        return nicknames;
    }

    public boolean existsUserById(String userId) {
        return userRepository.existsById(userId);
    }

    public User loadByUserId(String userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public Map<String, OnlineRequestDto> getUserOnlineDetails(List<String> userIds) {
        return userService.getUserOnlineDetails(userIds);
    }
}
