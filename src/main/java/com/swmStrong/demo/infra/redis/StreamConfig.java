package com.swmStrong.demo.infra.redis;

import lombok.Getter;

@Getter
public enum StreamConfig {
    LEADERBOARD("usage_log_stream", "leaderboard_group");

    private final String streamKey;
    private final String group;

    StreamConfig(String streamKey, String group) {
        this.streamKey = streamKey;
        this.group = group;
    }
}
