package com.swmStrong.demo.config.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Map<String, Object> responseMap = new HashMap<>();

        AuthenticationException filterException = (AuthenticationException) request.getAttribute("exception");
        String message = filterException != null ? filterException.getMessage() : authException.getMessage();
        
        // Determine appropriate error code based on the situation
        String errorCode;
        int statusCode = HttpServletResponse.SC_UNAUTHORIZED;
        
        // Check if this is a request that requires authentication but validation might fail
        String method = request.getMethod();
        boolean isBodyRequest = "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method);
        
        // If the request has a body and no valid token, inform that authentication is required first
        if (isBodyRequest && message != null && message.contains("인증 정보가 없습니다")) {
            errorCode = "4010"; // _UNAUTHORIZED
            message = "인증이 필요합니다. 유효한 토큰을 제공해주세요.";
        } else {
            errorCode = "4011"; // _INVALID_TOKEN
        }
        
        response.setStatus(statusCode);
        responseMap.put("isSuccess", false);
        responseMap.put("code", errorCode);
        responseMap.put("message", message);
        responseMap.put("data", null);

        objectMapper.writeValue(response.getOutputStream(), responseMap);
    }
}