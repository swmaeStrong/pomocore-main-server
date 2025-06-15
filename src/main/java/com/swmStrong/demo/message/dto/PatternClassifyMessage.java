package com.swmStrong.demo.message.dto;

import lombok.Builder;

public record PatternClassifyMessage(
        String usageLogId,
        String app,
        String title,
        String domain
) {
    @Builder
    public PatternClassifyMessage(String usageLogId, String app, String title, String domain) {
        this.usageLogId = usageLogId;
        this.app = app;
        this.title = title;
        this.domain = domain;
    }
}
