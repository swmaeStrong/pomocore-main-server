package com.swmStrong.demo.infra.token;

import lombok.Getter;

@Getter
public enum TokenType {
    accessToken((long) 60 * 10), // 10min
    refreshToken((long) 60 * 60 * 24 * 365 * 100); // 100year

    private final Long expireTime;

    TokenType(Long expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
