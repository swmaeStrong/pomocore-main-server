package com.swmStrong.demo.common.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements BaseCode {

    _OK("2000", "요청이 성공적으로 처리되었습니다.", HttpStatus.OK),
    _CREATED("2010", "리소스가 성공적으로 생성되었습니다.", HttpStatus.CREATED),
    _NO_CONTENT("2040", "내용이 없습니다.", HttpStatus.NO_CONTENT);


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
