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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Slf4j
@Component
public class GeminiClassifier implements LLMClassifier {
    private final GeminiConfig geminiConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AtomicInteger currentKeyIndex = new AtomicInteger(0);

    public GeminiClassifier(GeminiConfig geminiConfig) {
        this.geminiConfig = geminiConfig;
    }

    private HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }


    @Override
    public String classify(String query) {
        String prompt = getPrompt(query);

        // Gemini API 요청용 body 구성
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = setHeaders();

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        List<String> apiKeys = geminiConfig.keys();
        if (apiKeys == null || apiKeys.isEmpty()) {
            log.error("No API keys configured");
            return null;
        }

        // Try each API key
        for (int attempt = 0; attempt < apiKeys.size(); attempt++) {
            int keyIndex = currentKeyIndex.getAndIncrement() % apiKeys.size();
            String currentApiKey = apiKeys.get(keyIndex);
            
            try {
                ResponseEntity<Map> response = restTemplate.postForEntity(
                        geminiConfig.url() + "?key=" + currentApiKey,
                        requestEntity,
                        Map.class
                );

                Map<?, ?> body = response.getBody();
                if (body == null) {
                    log.warn("Empty response from Gemini API with key index {}", keyIndex);
                    continue;
                }
                
                List<?> candidates = (List<?>) body.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                    Map<?, ?> contentResp = (Map<?, ?>) firstCandidate.get("content");
                    List<?> parts = (List<?>) contentResp.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map<?, ?> partResp = (Map<?, ?>) parts.get(0);
                        String result = ((String) partResp.get("text")).trim();
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
                log.error("Error calling Gemini API with key index {}: {}", keyIndex, e.getMessage());
            }
        }
        
        log.error("All API keys exhausted or failed");
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
