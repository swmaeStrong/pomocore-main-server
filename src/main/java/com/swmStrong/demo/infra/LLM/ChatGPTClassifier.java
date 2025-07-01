package com.swmStrong.demo.infra.LLM;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Component
public class ChatGPTClassifier implements LLMClassifier {
    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);

    public ChatGPTClassifier(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
    }

    private HttpHeaders setHeaders(String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }


    @Override
    public String classify(String query) {
        Map<String, Object> requestBody = getStringObjectMap(query);

        List<String> apiKeys = openAIConfig.keys();
        if (apiKeys == null || apiKeys.isEmpty()) {
            log.error("No API keys configured for OpenAI");
            return null;
        }

        for (int attempt = 0; attempt < apiKeys.size(); attempt++) {
            int keyIndex = currentKeyIndex.getAndIncrement() % apiKeys.size();
            String currentApiKey = apiKeys.get(keyIndex);
            
            HttpHeaders headers = setHeaders(currentApiKey);
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        openAIConfig.url(),
                        requestEntity,
                        Map.class
                );

                Map<?, ?> body = response.getBody();
                if (body == null) {
                    log.warn("Empty response from OpenAI API with key index {}", keyIndex);
                    continue;
                }
                
                List<?> choices = (List<?>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> messageResp = (Map<?, ?>) firstChoice.get("message");
                    if (messageResp != null) {
                        String result = ((String) messageResp.get("content")).trim();
                        log.info("Successfully classified using API key index {}. result: {}", keyIndex, result);
                        return result;
                    }
                }

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || 
                    e.getStatusCode() == HttpStatus.FORBIDDEN ||
                    e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    log.warn("API key index {} exhausted or invalid: {}", keyIndex, e.getMessage());
                    // Continue to next key
                } else {
                    log.error("Unexpected error with API key index {}: {}", keyIndex, e.getMessage());
                    // For other errors, we might want to retry with the same key
                }
            } catch (Exception e) {
                log.error("Error calling OpenAI API with key index {}: {}", keyIndex, e.getMessage());
            }
        }
        
        log.error("All OpenAI API keys exhausted or failed");
        return null;
    }

    private Map<String, Object> getStringObjectMap(String query) {
        String prompt = getPrompt(query);

        // OpenAI API 요청용 body 구성
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", openAIConfig.model());
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0);
        requestBody.put("max_tokens", 50);
        return requestBody;
    }

    private String getPrompt(String query) {
        return
"""
Which category does the usage of this app with the given URL and title belong to?
Respond with **only one** of the following categories. **Do not explain. Do not say anything else.**
                
# category
SNS, YouTube, Documentation, Design, Communication, LLM, Development, Productivity, Video Editing, Entertainment, File Management, System & Utilities, Game, Education, Finance

# input
%s
""".formatted(query);
    }
}