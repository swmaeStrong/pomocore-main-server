package com.swmStrong.demo.infra.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class JsonLoader {

    private final ObjectMapper objectMapper;

    public JsonLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public <T> T load(String location, Class<T> c) {
        System.out.println("잘되냐?");
        try {
            return objectMapper.readValue(new ClassPathResource(location).getInputStream(), c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
