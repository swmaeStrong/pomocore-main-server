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


@Slf4j
@Component
public class ChatGPTClassifier implements LLMClassifier {
    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public ChatGPTClassifier(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
    }

    private HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAIConfig.key());
        return headers;
    }


    @Override
    public String classify(String query) {
        String prompt = getPrompt(query);

        // OpenAI API 요청용 body 구성
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0);
        requestBody.put("max_tokens", 50);

        if (openAIConfig.key() == null || openAIConfig.key().isEmpty()) {
            log.error("No API key configured for OpenAI");
            return null;
        }

        HttpHeaders headers = setHeaders();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    openAIConfig.url(),
                    requestEntity,
                    Map.class
            );

            Map<?, ?> body = response.getBody();
            if (body == null) {
                log.warn("Empty response from OpenAI API");
                return null;
            }
            
            List<?> choices = (List<?>) body.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                Map<?, ?> messageResp = (Map<?, ?>) firstChoice.get("message");
                if (messageResp != null) {
                    String result = ((String) messageResp.get("content")).trim();
                    log.info("Successfully classified with ChatGPT. result: {}", result);
                    return result;
                }
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("ChatGPT API rate limit exceeded: {}", e.getMessage());
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN ||
                       e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                log.error("ChatGPT API key invalid: {}", e.getMessage());
            } else {
                log.error("Unexpected error calling ChatGPT API: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error calling ChatGPT API: {}", e.getMessage());
        }
        
        return null;
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