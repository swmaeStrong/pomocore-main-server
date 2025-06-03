package com.swmStrong.demo.common.exception;
import com.swmStrong.demo.common.response.ApiResponse;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.common.response.CustomResponseEntity;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Optional;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException e) {
        ErrorCode code = e.getErrorCode();
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
}
