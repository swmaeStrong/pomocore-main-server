package com.swmStrong.demo.config.security.filter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.common.response.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (HttpMessageNotReadableException e) {
            sendErrorResponse(response, ErrorCode._VALIDATION_ERROR, "Invalid request format");
        } catch (Exception e) {
            if (e.getCause() instanceof JsonParseException ||
                e.getCause() instanceof JsonMappingException) {
                sendErrorResponse(response, ErrorCode._VALIDATION_ERROR, "JSON parsing error");
            } else {
                throw e;
            }
        }
    }

    private void sendErrorResponse(HttpServletResponse response,
                                  ErrorCode errorCode,
                                  String message) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.fail(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}