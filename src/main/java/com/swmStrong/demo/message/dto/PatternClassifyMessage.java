package com.swmStrong.demo.message.dto;

import lombok.Builder;

public record PatternClassifyMessage(
        String usageLogId,
        String app,
        String title,
        String url
) {
    @Builder
    public PatternClassifyMessage(String usageLogId, String app, String title, String url) {
        this.usageLogId = usageLogId;
        this.app = app;
        this.title = title;
        this.url = url;
    }
}
