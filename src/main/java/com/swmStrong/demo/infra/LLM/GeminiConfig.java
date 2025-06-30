package com.swmStrong.demo.infra.LLM;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "llm.gemini.api")
public record GeminiConfig(
    String url,
    List<String> keys
) {}