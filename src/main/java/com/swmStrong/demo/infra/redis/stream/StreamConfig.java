package com.swmStrong.demo.infra.redis.stream;

import lombok.Getter;

@Getter
public enum StreamConfig {
    LEADERBOARD("pattern_match_stream", "leaderboard_group", "leaderboard_consumer"),
    PATTERN_MATCH("usage_log_stream", "pattern_match_group", "pattern_match_consumer"),
    POMODORO_PATTERN_MATCH("pomodoro_usage_log_stream", "pomodoro_pattern_match_group", "pomodoro_pattern_match_consumer");

    private final String streamKey;
    private final String group;
    private final String consumer;

    StreamConfig(String streamKey, String group, String consumer) {
        this.streamKey = streamKey;
        this.group = group;
        this.consumer = consumer;
    }
}
