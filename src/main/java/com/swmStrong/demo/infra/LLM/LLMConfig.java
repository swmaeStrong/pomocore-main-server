package com.swmStrong.demo.infra.LLM;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "llm.api")
public record LLMConfig(
    String url,
    List<String> keys
) {}