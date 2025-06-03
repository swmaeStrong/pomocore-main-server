package com.swmStrong.demo.common.response;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.common.exception.code.SuccessCode;


public record ApiResponse<T>(
        Boolean isSuccess,
        String code,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(SuccessCode code, T data) {
        return new ApiResponse<>(true, code.getCode(), code.getMessage(), data);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(false, errorCode.getCode(), message, null);
    }
}
