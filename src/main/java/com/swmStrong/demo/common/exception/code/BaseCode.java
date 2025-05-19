package com.swmStrong.demo.common.exception.code;

import org.springframework.http.HttpStatus;

public interface BaseCode {
    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}
