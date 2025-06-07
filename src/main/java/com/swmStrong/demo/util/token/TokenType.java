package com.swmStrong.demo.util.token;

import lombok.Getter;

@Getter
public enum TokenType {
    accessToken(-1L), //TODO: 프로덕션 환경에서 반드시 바꿀 것
    refreshToken(-1L);

    private final Long expireTime;

    TokenType(Long expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
