package com.swmStrong.demo.infra.LLM;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClassifier implements LLMClassifier {

    @Value("${llm.api.key}")
    private String apiKey;
    @Value("${llm.api.url}")
    private String URL;
    private final RestTemplate restTemplate = new RestTemplate();

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

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    URL + "?key=" + apiKey,
                    requestEntity,
                    Map.class
            );

            Map<?, ?> body = response.getBody();
            System.out.println(body);
            if (body == null) return null;
            List<?> candidates = (List<?>) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> contentResp = (Map<?, ?>) firstCandidate.get("content");
                List<?> parts = (List<?>) contentResp.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    Map<?, ?> partResp = (Map<?, ?>) parts.get(0);
                    return ((String) partResp.get("text")).trim();
                }
            }

        } catch (Exception e) {
            //TODO: 재시도 로직 또는 에러 수집 로직
            e.printStackTrace();
        }
        return null;
    }

    private String getPrompt(String query) {
        return
                """
Which category does the usage of this app with the given URL and title belong to?
Respond with **only one** of the following categories. **Do not explain. Do not say anything else.**
                
- Development
- LLM
- Entertainment
- Game
- Productivity
- Documentation
- SNS
- Design
- Communication
- Browsing
- Uncategorized
                
                App information: %s
                """.formatted(query);
    }
}
