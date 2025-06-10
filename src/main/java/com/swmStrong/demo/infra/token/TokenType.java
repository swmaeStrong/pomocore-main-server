package com.swmStrong.demo.infra.token;

import lombok.Getter;

@Getter
public enum TokenType {
    accessToken((long) 60 * 60 * 24 * 365 * 100), //TODO: 프로덕션 환경에서 반드시 바꿀 것
    refreshToken((long) 60 * 60 * 24 * 365 * 100);

    private final Long expireTime;

    TokenType(Long expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
