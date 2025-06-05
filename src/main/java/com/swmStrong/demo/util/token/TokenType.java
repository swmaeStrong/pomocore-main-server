package com.swmStrong.demo.util.token;

import lombok.Getter;

@Getter
public enum TokenType {
    accessToken((long) (60 * 60)),          // 60sec * 60min
    refreshToken(-1L);

    private final Long expireTime;

    TokenType(Long expireTime) {
        this.expireTime = expireTime;
    }

    @Override
    public String toString() {
        return switch (this) {
            case accessToken -> "accessToken";
            case refreshToken -> "refreshToken";
        };
    }
}