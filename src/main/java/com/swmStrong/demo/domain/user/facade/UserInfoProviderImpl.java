package com.swmStrong.demo.domain.user.facade;

import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInfoProviderImpl implements UserInfoProvider {

    private final UserRepository userRepository;

    public UserInfoProviderImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String loadNicknameByUserId(String userId) {
        return userRepository.findById(userId)
                .map(User::getNickname)
                .orElse("Unknown");
    }

    @Override
    public Map<String, String> loadNicknamesByUserIds(List<String> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        Map<String, String> nicknames = new HashMap<>();
        for (User user : users) {
            nicknames.put(user.getId(), user.getNickname());
        }
        return nicknames;
    }

    @Override
    public boolean existsUserById(String userId) {
        return userRepository.existsById(userId);
    }
}
