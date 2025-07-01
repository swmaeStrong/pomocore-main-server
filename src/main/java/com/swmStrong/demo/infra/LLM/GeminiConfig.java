package com.swmStrong.demo.infra.LLM;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "llm.gemini.api")
public record GeminiConfig(
    String url,
    List<String> keys,
    Map<String, Integer> keyWeights
) {
    public GeminiConfig {
        if (keyWeights == null) {
            keyWeights = Map.of();
        }
    }
}