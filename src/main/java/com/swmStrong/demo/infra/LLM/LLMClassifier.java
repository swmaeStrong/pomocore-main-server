package com.swmStrong.demo.infra.LLM;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LLMClassifier extends ChatGPTManager {
    protected final PromptTemplate promptTemplate;

    protected LLMClassifier(PromptTemplate promptTemplate, OpenAIConfig openAIConfig) {
        super(openAIConfig);
        this.promptTemplate = promptTemplate;
    }

    @Override
    protected String getPrompt(String query) {
        return promptTemplate.getClassificationPrompt(query);
    }
}