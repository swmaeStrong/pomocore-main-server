package com.swmStrong.demo.domain.user.repository;

import com.swmStrong.demo.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByNickname(String nickname);

    Optional<User> findByNickname(String nickname);
}
