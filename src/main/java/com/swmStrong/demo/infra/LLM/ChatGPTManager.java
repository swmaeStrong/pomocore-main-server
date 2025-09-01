package com.swmStrong.demo.infra.LLM;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public abstract class ChatGPTManager extends AbstractLLMManager {
    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public ChatGPTManager(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
    }

    protected String callApi(String query, String apiKey) {
        Map<String, Object> requestBody = buildRequestBody(query);

        HttpHeaders headers = createHeaders();
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

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
                log.trace("Successfully classified using ChatGPT. result: {}", result);
                return result;
            }
        }
        return null;
    }

    private Map<String, Object> buildRequestBody(String query) {
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
        requestBody.put("max_tokens", 150);
        return requestBody;
    }
    protected abstract String getPrompt(String query);

    @Override
    protected String getApiKey() {
        return openAIConfig.key();
    }

    @Override
    protected String getServiceName() {
        return "OpenAI";
    }
}
