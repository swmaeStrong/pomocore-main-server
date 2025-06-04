package com.swmStrong.demo.domain.common.enums;

public enum Role {
    UNREGISTERED, USER, ADMIN;

    public String getAuthority() {
        return this.name();
    }
}
