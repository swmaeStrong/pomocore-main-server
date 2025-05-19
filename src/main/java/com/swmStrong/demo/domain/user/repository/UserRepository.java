package com.swmStrong.demo.domain.user.repository;

import com.swmStrong.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Boolean existsByNickname(String nickname);
    Boolean existsByDeviceId(String deviceId);
}
