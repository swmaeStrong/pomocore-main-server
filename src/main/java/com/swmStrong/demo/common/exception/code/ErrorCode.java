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
    REQUEST_TIME_IS_FUTURE("4004", "미래 시간의 로그는 저장할 수 없습니다.", HttpStatus.BAD_REQUEST),
    REQUEST_TIME_CONTAIN_BEFORE_SAVED("4005", "저장하려는 로그는 이전의 로그 시간과 겹칩니다.", HttpStatus.BAD_REQUEST),
    REQUEST_TIME_IS_OVER_FUTURE("4006", "저장하려는 로그는 미래시간을 포함합니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE("4007", "유효하지 않은 파일입니다.", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("4008", "파일 크기가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),
    INVALID_FILE_TYPE("4009", "지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
    BAD_WORD_FILTER("bad_word", "금지단어가 포함되어 있습니다.", HttpStatus.BAD_REQUEST),
    INVALID_KEY("400B", "유효하지 않은 키값입니다.", HttpStatus.BAD_REQUEST),
    GROUP_OWNER_CANT_QUIT("400C", "그룹장은 탈퇴할 수 없습니다.", HttpStatus.BAD_REQUEST),
    GROUP_HAS_USER("400D", "삭제하려는 그룹에 유저가 남아있습니다.",  HttpStatus.BAD_REQUEST),
    INVALID_NICKNAME("400E", "사용할 수 없는 닉네임입니다.", HttpStatus.BAD_REQUEST),
    PASSWORD_NEEDED("password_needed", "비밀번호 입력이 필요합니다.", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD("incorrect_password", "비밀번호가 틀렸습니다.", HttpStatus.BAD_REQUEST),

    // --- 401 UNAUTHORIZED ---
    _UNAUTHORIZED("4010", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    _INVALID_TOKEN("4011", "토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    _SECURITY("401Z", "시큐리티 관련 오류입니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_AUTHORIZATION_FAILED("4013", "토큰이 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),

    // --- 403 FORBIDDEN ---
    _FORBIDDEN("4030", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    GROUP_OWNER_ONLY("4031", "그룹장만 접근할 수 있습니다.",  HttpStatus.FORBIDDEN),

    // --- 404 NOT FOUND ---
    _NOT_FOUND("4040", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("4041", "존재하지 않는 유저입니다", HttpStatus.NOT_FOUND),
    SUBSCRIPTION_PLAN_NOT_FOUND("4042", "존재하지 않는 구독 플랜입니다", HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND("4044", "존재하지 않는 카테고리입니다.", HttpStatus.NOT_FOUND),
    PATTERN_NOT_FOUND("4045", "존재하지 않는 패턴입니다.", HttpStatus.NOT_FOUND),
    BILLING_KEY_NOT_FOUND("4046", "존재하지 않는 빌링키입니다.", HttpStatus.NOT_FOUND),
    PAYMENT_METHOD_NOT_FOUND("4047", "존재하지 않는 결제 수단입니다.", HttpStatus.NOT_FOUND),
    USER_SUBSCRIPTION_NOT_FOUND("4048", "구독 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    NICKNAME_NOT_FOUND("4049", "해당하는 닉네임을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USAGE_LOG_NOT_FOUND("404A", "해당하는 사용 기록을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    GROUP_NOT_FOUND("404B", "해당하는 그룹을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    GROUP_USER_NOT_FOUND("404C", "해당 그룹에서 유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // --- 409 CONFLICT ---
    _CONFLICT("4090", "이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("dup_nickname", "이미 존재하는 닉네임입니다.", HttpStatus.CONFLICT),
    DUPLICATE_USER_ID("4092", "이미 등록된 유저 아이디입니다.", HttpStatus.CONFLICT),
    USER_ALREADY_REGISTERED("4093", "이미 가입된 회원입니다.", HttpStatus.CONFLICT),
    DUPLICATE_CATEGORY("4094", "이미 존재하는 카테고리입니다.", HttpStatus.CONFLICT),
    DUPLICATE_BILLING_KEY("4095", "이미 등록된 결제 수단입니다.", HttpStatus.CONFLICT),
    DUPLICATE_USER_SUBSCRIPTION("4096", "이미 구독중인 플랜이 있습니다.", HttpStatus.CONFLICT),
    DUPLICATE_USER_EMAIL("4097", "이미 가입된 이메일입니다.", HttpStatus.CONFLICT),
    USER_ALREADY_REGISTERED_BY_SOCIAL_LOGIN("4098", "이미 가입된 소셜 로그인 계정이 존재합니다.", HttpStatus.CONFLICT),
    GROUP_ALREADY_JOINED("4099", "이미 속해있는 그룹입니다.",  HttpStatus.CONFLICT),
    GROUP_NAME_ALREADY_EXISTS("409A", "이미 존재하는 그룹 이름입니다.", HttpStatus.CONFLICT),

    // --- 429 TO MANY REQUESTS ---
    IP_RATE_LIMIT_EXCEEDED("4290", "지속적인 요청으로 인해 차단되었습니다. 악의적 접근으로 간주될 수 있습니다.", HttpStatus.TOO_MANY_REQUESTS),

    // --- 500 INTERNAL SERVER ERROR ---
    _INTERNAL_SERVER_ERROR("5000", "서버 내부 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_FAILED("5001", "결제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_CANCELLATION_FAILED("5002", "결제 취소가 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    LOG_SAVE_FAILED("5003", "로그 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_SEND_FAILED("5004", "이메일 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_UPLOAD_ERROR("5005", "파일 업로드 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // --- 502 BAD GATEWAY ---
    EXTERNAL_SERVICE_ERROR("5020", "외부 서비스 연동 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
