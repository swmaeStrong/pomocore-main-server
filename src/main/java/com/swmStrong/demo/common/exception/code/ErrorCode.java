package com.swmStrong.demo.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements BaseCode {
    // --- 400 BAD REQUEST ---
    _BAD_REQUEST("4000", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    _VALIDATION_ERROR("4001", "입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

    _UNAUTHORIZED("4010", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    _INVALID_TOKEN("4011", "토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),

    _FORBIDDEN("4030", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    _NOT_FOUND("4040", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    _CONFLICT("4090", "이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),

    _INTERNAL_SERVER_ERROR("5000", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),


    // 커스텀 에러
    DUPLICATE_NICKNAME("4091", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT),
    DUPLICATE_DEVICE_ID("4092", "이미 등록된 기기입니다.", HttpStatus.CONFLICT);



    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
