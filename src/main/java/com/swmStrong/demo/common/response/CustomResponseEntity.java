package com.swmStrong.demo.common.response;

import com.swmStrong.demo.common.exception.code.SuccessCode;
import org.springframework.http.ResponseEntity;

public class CustomResponseEntity {

    public static <T> ResponseEntity<ApiResponse<T>> of(SuccessCode successCode, T data) {
        return ResponseEntity
                .status(successCode.getHttpStatus())
                .body(ApiResponse.success(successCode, data));
    }

    public static ResponseEntity<ApiResponse<Void>> of(SuccessCode successCode) {
        return of(successCode, null);
    }
}
