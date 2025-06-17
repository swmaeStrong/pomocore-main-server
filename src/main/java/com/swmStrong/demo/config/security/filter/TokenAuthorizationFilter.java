package com.swmStrong.demo.config.security.filter;

import com.swmStrong.demo.config.security.WhiteListConfig;
import com.swmStrong.demo.infra.token.TokenManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
public class TokenAuthorizationFilter extends OncePerRequestFilter {

    private final TokenManager tokenManager;

    public TokenAuthorizationFilter(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if ("GET".equalsIgnoreCase(request.getMethod()) &&
                Arrays.stream(WhiteListConfig.WHITE_LIST_FOR_GET)
                        .anyMatch(pattern -> new AntPathRequestMatcher(pattern).matches(request))
        ) {
            log.info("passed by WhiteList: {}", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
        }
        // 토큰 처리 방식 변경
        // accessToken 을 헤더에서 가져온 후,
        // 토큰이 유효한 경우 필터를 통과
        // 토큰이 유효하지 않은 경우 리프레시 해야한다는 응답을 반환
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.error("Authorization header is empty. requestURI: {}", request.getRequestURI());
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        String accessToken = authorizationHeader.substring(7); // 'Bearer '이후의 토큰 값만 추출

        // 토큰 검증 로직
        if (tokenManager.isTokenValid(accessToken)) {
            tokenManager.authenticateWithToken(accessToken);
            filterChain.doFilter(request, response);
        } else {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }
    }
}