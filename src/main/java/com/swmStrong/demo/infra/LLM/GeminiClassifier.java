package com.swmStrong.demo.infra.LLM;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class GeminiClassifier extends AbstractLLMClassifier {
    private final GeminiConfig geminiConfig;

    public GeminiClassifier(GeminiConfig geminiConfig, PromptTemplate promptTemplate) {
        super(promptTemplate);
        this.geminiConfig = geminiConfig;
    }

    @Override
    protected String callApi(String query, String apiKey, int keyIndex) throws Exception {
        Map<String, Object> requestBody = buildRequestBody(query);
        
        HttpHeaders headers = createHeaders();
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
                geminiConfig.url() + "?key=" + apiKey,
                requestEntity,
                Map.class
        );

        Map<?, ?> body = response.getBody();
        if (body == null) {
            log.warn("Empty response from Gemini API with key index {}", keyIndex);
            return null;
        }
        
        List<?> candidates = (List<?>) body.get("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
            Map<?, ?> contentResp = (Map<?, ?>) firstCandidate.get("content");
            List<?> parts = (List<?>) contentResp.get("parts");
            if (parts != null && !parts.isEmpty()) {
                Map<?, ?> partResp = (Map<?, ?>) parts.get(0);
                String result = ((String) partResp.get("text")).trim();
                log.trace("Successfully classified using Gemini API key index {}. result: {}", keyIndex, result);
                return result;
            }
        }
        
        return null;
    }

    private Map<String, Object> buildRequestBody(String query) {
        String prompt = promptTemplate.getClassificationPrompt(query);

        // Gemini API 요청용 body 구성
        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));
        
        return requestBody;
    }

    @Override
    protected List<String> getApiKeys() {
        return geminiConfig.keys();
    }

    @Override
    protected Map<String, Integer> getKeyWeights() {
        return geminiConfig.keyWeights();
    }

    @Override
    protected String getKeyPrefix() {
        return "GEMINI_API_KEY_";
    }

    @Override
    protected String getServiceName() {
        return "Gemini";
    }
}