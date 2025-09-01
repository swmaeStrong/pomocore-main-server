package com.swmStrong.demo.infra.LLM;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;


@Slf4j
public abstract class AbstractLLMManager {

    private final int CALLBACK_LIMIT = 5;

    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public String getResult(String query) {
        String key = getApiKey();
        int attempt = 0;
        while (attempt < CALLBACK_LIMIT) {
            attempt ++;

            try {
                String result = callApi(query, key);
                if (result != null) {
                    return result;
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS ||
                        e.getStatusCode() == HttpStatus.FORBIDDEN ||
                        e.getStatusCode() == HttpStatus.UNAUTHORIZED
                ) {
                    log.error("API key exhausted or invalid: {}", e.getMessage());
                } else {
                    log.error("Unexpected error with API: {}", e.getMessage());
                }
            } catch (Exception e) {
                log.error("Error calling {} API with key: {}", getServiceName(), e.getMessage());
            }
        }

        log.error("all {} request exhausted or failed", CALLBACK_LIMIT);
        return null;
    }

    protected abstract String callApi(String query, String apiKey);
    protected abstract String getApiKey();
    protected abstract String getServiceName();
}
