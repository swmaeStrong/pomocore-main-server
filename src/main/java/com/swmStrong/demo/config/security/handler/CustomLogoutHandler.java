package com.swmStrong.demo.config.security.handler;

import com.swmStrong.demo.util.redis.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.swmStrong.demo.util.redis.RedisUtil.REDIS_REFRESH_TOKEN_PREFIX;

public class CustomLogoutHandler implements LogoutHandler {

    private final RedisUtil redisUtil;

    public CustomLogoutHandler(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        String email = authentication.getName();
        redisUtil.deleteData(REDIS_REFRESH_TOKEN_PREFIX+email);
    }
}