package com.swmStrong.demo.infra.LLM;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "llm.openai.api")
public record OpenAIConfig(
    String url,
    List<String> keys,
    String model
) {}