package com.swmStrong.demo.common.exception;

import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import io.sentry.Sentry;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.Optional;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        ErrorCode code = e.getErrorCode();

        if (code.getHttpStatus().is5xxServerError()) {
            Sentry.captureException(e);
        }
        
        return ResponseEntity
                .status(code.getHttpStatus())
                .body(ApiResponse.fail(code));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode._VALIDATION_ERROR;

        String message = Optional.ofNullable(e.getBindingResult().getFieldError())
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse(errorCode.getMessage());

        return CustomResponseEntity.of(errorCode, message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorCode errorCode = ErrorCode._VALIDATION_ERROR;
        return CustomResponseEntity.of(errorCode, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException e) {
        return CustomResponseEntity.of(
                ErrorCode._UNAUTHORIZED
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        return CustomResponseEntity.of(
                ErrorCode._FORBIDDEN
        );
    }
}
