package com.swmStrong.demo.config.security.handler;

import com.swmStrong.demo.infra.redis.repository.RedisRepositoryImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.swmStrong.demo.infra.redis.repository.RedisRepositoryImpl.REDIS_REFRESH_TOKEN_PREFIX;

public class CustomLogoutHandler implements LogoutHandler {

    private final RedisRepositoryImpl redisRepository;

    public CustomLogoutHandler(RedisRepositoryImpl redisRepository) {
        this.redisRepository = redisRepository;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String email = authentication.getName();
        redisRepository.deleteData(REDIS_REFRESH_TOKEN_PREFIX+email);
    }
}