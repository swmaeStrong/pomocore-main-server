package com.swmStrong.demo.common.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.common.exception.code.SuccessCode;

@JsonPropertyOrder({"isSuccess", "code", "message", "result"})

public record ApiResponse<T>(
        @JsonProperty("isSuccess")boolean isSuccess,
        String code,
        String message,
        @JsonProperty("result")T data
) {
    public static <T> ApiResponse<T> success(SuccessCode code, T data) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }
}
