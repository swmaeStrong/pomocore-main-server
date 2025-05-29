package com.swmStrong.demo.domain.global;

public enum Role {
    UNREGISTERED, USER, ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}
