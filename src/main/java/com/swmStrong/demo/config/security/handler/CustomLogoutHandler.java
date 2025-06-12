package com.swmStrong.demo.config.security.handler;

import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import com.swmStrong.demo.message.event.UnregisteredUserLogoutEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.swmStrong.demo.infra.redis.repository.RedisRepositoryImpl.REDIS_REFRESH_TOKEN_PREFIX;

public class CustomLogoutHandler implements LogoutHandler {

    private final RedisRepository redisRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public CustomLogoutHandler(
            RedisRepository redisRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.redisRepository = redisRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {

        SecurityPrincipal securityPrincipal = (SecurityPrincipal) authentication.getPrincipal();
        String userId = securityPrincipal.userId();
        redisRepository.deleteData(REDIS_REFRESH_TOKEN_PREFIX+userId);

        if (securityPrincipal.getAuthorities().contains(new SimpleGrantedAuthority("UNREGISTERED"))) {
            applicationEventPublisher.publishEvent(UnregisteredUserLogoutEvent.of(userId));
        }
    }
}