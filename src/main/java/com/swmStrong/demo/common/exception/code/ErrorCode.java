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
    USER_MISMATCH("4002", "토큰 생성 에러: 회원 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    PERIOD_KEY_ERROR("4003", "해당 기간 키값은 유효하지 않습니다.", HttpStatus.BAD_REQUEST),

    // --- 401 UNAUTHORIZED ---
    _UNAUTHORIZED("4010", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    _INVALID_TOKEN("4011", "토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    _SECURITY("401Z", "시큐리티 관련 오류입니다.", HttpStatus.UNAUTHORIZED),

    // --- 403 FORBIDDEN ---
    _FORBIDDEN("4030", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // --- 404 NOT FOUND ---
    _NOT_FOUND("4040", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("4041", "존재하지 않는 유저입니다", HttpStatus.NOT_FOUND),
    SUBSCRIPTION_PLAN_NOT_FOUND("4042", "존재하지 않는 구독 플랜입니다", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("4044", "존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND),
    PATTERN_NOT_FOUND("4045", "존재하지 않는 패턴입니다.", HttpStatus.NOT_FOUND),
    BILLING_KEY_NOT_FOUND("4046", "존재하지 않는 빌링키입니다.", HttpStatus.NOT_FOUND),
    PAYMENT_METHOD_NOT_FOUND("4047", "존재하지 않는 결제 수단입니다.", HttpStatus.NOT_FOUND),
    USER_SUBSCRIPTION_NOT_FOUND("4048", "구독 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),

    // --- 409 CONFLICT ---
    _CONFLICT("4090", "이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("4091", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT),
    DUPLICATE_USER_ID("4092", "이미 등록된 유저 아이디입니다.", HttpStatus.CONFLICT),
    USER_ALREADY_REGISTERED("4093", "이미 가입된 회원입니다.", HttpStatus.CONFLICT),
    DUPLICATE_CATEGORY("4094", "이미 존재하는 카테고리입니다.", HttpStatus.CONFLICT),
    DUPLICATE_BILLING_KEY("4095", "이미 등록된 결제 수단입니다.", HttpStatus.CONFLICT),
    DUPLICATE_USER_SUBSCRIPTION("4096", "이미 구독중인 플랜이 있습니다.", HttpStatus.CONFLICT),
    DUPLICATE_USER_EMAIL("4097", "이미 가입된 이메일입니다.", HttpStatus.CONFLICT),

    // --- 500 INTERNAL SERVER ERROR ---
    _INTERNAL_SERVER_ERROR("5000", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_FAILED("5001", "결제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_CANCELLATION_FAILED("5002", "결제 취소가 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
