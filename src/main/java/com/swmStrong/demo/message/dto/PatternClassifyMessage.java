package com.swmStrong.demo.message.dto;

import lombok.Builder;

public record PatternClassifyMessage(
        String usageLogId,
        String app,
        String title,
        String url,
        String categoryId,
        Double margin
) {
    @Builder
    public PatternClassifyMessage(String usageLogId, String app, String title, String url, String categoryId, Double margin) {
        this.usageLogId = usageLogId;
        this.app = app;
        this.title = title;
        this.url = url;
        this.categoryId = categoryId;
        this.margin = margin;
    }
}
