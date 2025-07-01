package com.swmStrong.demo.infra.LLM;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "llm.openai.api")
public record OpenAIConfig(
    String url,
    List<String> keys,
    String model,
    Map<String, Integer> keyWeights
) {
    public OpenAIConfig {
        if (keyWeights == null) {
            keyWeights = Map.of();
        }
    }
}