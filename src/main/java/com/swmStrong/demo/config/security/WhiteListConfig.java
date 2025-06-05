package com.swmStrong.demo.config.security;

public class WhiteListConfig {
    private static final String PREFIX = "";

    public static final String[] WHITE_LIST = {
            PREFIX+"/auth/register",
            PREFIX+"/auth/login",
            PREFIX+"/guest-users/**",
            PREFIX+"/user/unregistered-token",
            PREFIX+"/v3/api-docs/**",
            PREFIX+"/swagger-ui/**",
            PREFIX+"/swagger-ui.html",
            PREFIX+"/leaderboard/**",
            PREFIX+"/webhook/**",
            "/favicon.ico"
    };

    public static final String[] WHITE_LIST_FOR_GET = {
            PREFIX+"/category",
            PREFIX+"/category/**",
            PREFIX+"/usage-log",
    };
}
