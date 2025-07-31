package com.swmStrong.demo.infra.redis.stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RedisStreamProducer {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public RedisStreamProducer(ObjectMapper objectMapper, StringRedisTemplate stringRedisTemplate) {
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public <T> void send(String streamKey, T dto) {
        Map<String, String> body = objectMapper.convertValue(dto, new TypeReference<>() {});
        stringRedisTemplate.opsForStream()
                .add(MapRecord.create(streamKey, body));
    }

    public <T> void sendBatch(String streamKey, List<T> dtos) {
        stringRedisTemplate.executePipelined((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
            for (T dto : dtos) {
                Map<String, String> body = objectMapper.convertValue(dto, new TypeReference<>() {});
                stringRedisTemplate.opsForStream().add(MapRecord.create(streamKey, body));
            }
            return null;
        });
    }
}
