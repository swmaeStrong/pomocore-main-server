package com.swmStrong.demo.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.config.security.principal.SecurityPrincipal;
import com.swmStrong.demo.domain.global.Role;
import com.swmStrong.demo.util.token.TokenType;
import com.swmStrong.demo.util.token.TokenUtil;
import com.swmStrong.demo.util.token.dto.TokenResponseDto;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final TokenUtil tokenUtil;
    private final ObjectMapper objectMapper;

    public CustomAuthenticationSuccessHandler(TokenUtil tokenUtil, ObjectMapper objectMapper) {
        this.tokenUtil = tokenUtil;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws ServletException, IOException {
        SecurityPrincipal principal = (SecurityPrincipal) authentication.getPrincipal();

        Role role = Role.valueOf(principal.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new IllegalStateException("권한이 존재하지 않습니다."))
        );

        String userAgent = request.getHeader("User-Agent");

        TokenResponseDto tokenResponseDto = tokenUtil.getToken(principal.getUserId(), userAgent, role);

        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put(TokenType.accessToken.toString(), tokenResponseDto.accessToken());
        responseBody.put(TokenType.refreshToken.toString(), tokenResponseDto.refreshToken());

        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
