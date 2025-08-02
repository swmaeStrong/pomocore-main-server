package com.swmStrong.demo.config.security;

public class WhiteListConfig {
    private static final String PREFIX = "";

    public static final String[] WHITE_LIST = {
            PREFIX+"/auth/login",
            PREFIX+"/auth/social-login",
            PREFIX+"/auth/refresh",
            PREFIX+"/user",
            PREFIX+"/user/get-token",
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
            PREFIX+"/usage-log/hour",
            PREFIX+"/usage-log/time-line",
            PREFIX+"/actuator/**",
            PREFIX+"/group/search"
    };
}
