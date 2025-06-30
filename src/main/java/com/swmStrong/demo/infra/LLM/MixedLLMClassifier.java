package com.swmStrong.demo.infra.LLM;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@Primary
public class MixedLLMClassifier implements LLMClassifier {
    private final GeminiClassifier geminiClassifier;
    private final ChatGPTClassifier chatGPTClassifier;
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    private static final int GEMINI_WEIGHT = 3;
    private static final int CHATGPT_WEIGHT = 1;
    private static final int TOTAL_WEIGHT = GEMINI_WEIGHT + CHATGPT_WEIGHT;

    public MixedLLMClassifier(GeminiClassifier geminiClassifier, ChatGPTClassifier chatGPTClassifier) {
        this.geminiClassifier = geminiClassifier;
        this.chatGPTClassifier = chatGPTClassifier;
    }

    @Override
    public String classify(String query) {
        int currentCount = requestCounter.getAndIncrement();
        int position = currentCount % TOTAL_WEIGHT;
        
        String result;

        if (position < CHATGPT_WEIGHT) {
            log.debug("Using ChatGPT classifier (request #{}, position {})", currentCount, position);
            result = chatGPTClassifier.classify(query);
            if (result != null) return result;

            log.trace("ChatGPT failed, falling back to Gemini");
            result = geminiClassifier.classify(query);
        } else {
            log.debug("Using Gemini classifier (request #{}, position {})", currentCount, position);
            result = geminiClassifier.classify(query);
            
            if (result != null) return result;

            log.trace("Gemini failed, falling back to ChatGPT");
            result = chatGPTClassifier.classify(query);
        }
        
        if (result == null) {
            log.error("Both Gemini and ChatGPT classifiers failed for query: {}", query);
        }
        return result;
    }
}