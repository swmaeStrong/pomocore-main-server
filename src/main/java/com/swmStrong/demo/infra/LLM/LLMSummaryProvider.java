package com.swmStrong.demo.infra.LLM;

import org.springframework.stereotype.Component;

@Component
public class LLMSummaryProvider extends ChatGPTManager{

    protected final PromptTemplate promptTemplate;

    public LLMSummaryProvider(OpenAIConfig openAIConfig, PromptTemplate promptTemplate) {
        super(openAIConfig);
        this.promptTemplate = promptTemplate;
    }

    protected String getPrompt(String query) {
        return promptTemplate.getSummarizeTemplate(query);
    }
}
