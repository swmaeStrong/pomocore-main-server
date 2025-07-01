package com.swmStrong.demo.infra.LLM;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class AbstractLLMClassifier implements LLMClassifier {
    protected final PromptTemplate promptTemplate;
    protected final RestTemplate restTemplate = new RestTemplate();
    protected final AtomicInteger currentKeyIndex = new AtomicInteger(0);
    protected final Random random = new Random();

    protected AbstractLLMClassifier(PromptTemplate promptTemplate) {
        this.promptTemplate = promptTemplate;
    }

    protected HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected int selectWeightedKeyIndex(List<String> apiKeys, Set<Integer> excludedIndices, Map<String, Integer> keyWeights, String keyPrefix) {
        if (keyWeights == null || keyWeights.isEmpty()) {
            for (int i = 0; i < apiKeys.size(); i++) {
                int index = (currentKeyIndex.getAndIncrement() + i) % apiKeys.size();
                if (!excludedIndices.contains(index)) {
                    return index;
                }
            }
            return -1;
        }

        List<Integer> weightedIndices = new ArrayList<>();
        for (int i = 0; i < apiKeys.size(); i++) {
            if (excludedIndices.contains(i)) {
                continue;
            }
            
            String keyName = keyPrefix + (i + 1);
            int weight = keyWeights.getOrDefault(keyName, 1);
            
            for (int j = 0; j < weight; j++) {
                weightedIndices.add(i);
            }
        }

        if (weightedIndices.isEmpty()) {
            return -1;
        }

        return weightedIndices.get(random.nextInt(weightedIndices.size()));
    }

    @Override
    public String classify(String query) {
        List<String> apiKeys = getApiKeys();
        if (apiKeys == null || apiKeys.isEmpty()) {
            log.error("No API keys configured for {}", getServiceName());
            return null;
        }

        Set<Integer> failedKeyIndices = new HashSet<>();
        
        for (int attempt = 0; attempt < apiKeys.size(); attempt++) {
            int keyIndex = selectWeightedKeyIndex(apiKeys, failedKeyIndices, getKeyWeights(), getKeyPrefix());
            
            if (keyIndex == -1) {
                log.error("No more available {} API keys to try", getServiceName());
                break;
            }
            
            String currentApiKey = apiKeys.get(keyIndex);
            
            try {
                String result = callApi(query, currentApiKey, keyIndex);
                if (result != null) {
                    return result;
                }
                failedKeyIndices.add(keyIndex);
                
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS || 
                    e.getStatusCode() == HttpStatus.FORBIDDEN ||
                    e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                    log.warn("API key index {} exhausted or invalid: {}", keyIndex, e.getMessage());
                    failedKeyIndices.add(keyIndex);
                } else {
                    log.error("Unexpected error with API key index {}: {}", keyIndex, e.getMessage());
                    failedKeyIndices.add(keyIndex);
                }
            } catch (Exception e) {
                log.error("Error calling {} API with key index {}: {}", getServiceName(), keyIndex, e.getMessage());
                failedKeyIndices.add(keyIndex);
            }
        }
        
        log.error("All {} API keys exhausted or failed", getServiceName());
        return null;
    }

    protected abstract String callApi(String query, String apiKey, int keyIndex) throws Exception;
    protected abstract List<String> getApiKeys();
    protected abstract Map<String, Integer> getKeyWeights();
    protected abstract String getKeyPrefix();
    protected abstract String getServiceName();
}