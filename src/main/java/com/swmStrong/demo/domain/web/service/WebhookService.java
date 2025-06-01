package com.swmStrong.demo.domain.web.service;

import java.util.Map;

public interface WebhookService {
    void handlePortOneWebhook(Map<String, Object> payload);
}
