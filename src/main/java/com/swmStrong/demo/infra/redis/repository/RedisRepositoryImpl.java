package com.swmStrong.demo.infra.redis.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;

@Slf4j
@Component
public class RedisRepositoryImpl implements RedisRepository {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisRepositoryImpl(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public Map<String, String> multiGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }
        
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        Map<String, String> result = new HashMap<>();
        
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = values != null && i < values.size() ? values.get(i) : null;
            if (value != null) {
                result.put(key, value);
            }
        }
        
        return result;
    }

    @Override
    public <T> void setDataWithExpire(String key, T value, long duration) {
        Duration expireDuration = Duration.ofSeconds(duration);
        redisTemplate.opsForValue().set(key, value.toString(), expireDuration);
    }

    @Override
    public <T> void setData(String key, T value) {
        redisTemplate.opsForValue().set(key, value.toString());
    }

    @Override
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public Long incrementWithExpireIfFirst(String key, long timeout, TimeUnit unit) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, timeout, unit);
        }
        return count;
    }

    @Override
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    @Override
    public Set<String> findKeys(String regex) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(regex).count(1000).build();
        
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        
        return keys;
    }

    @Override
    public void setJsonData(String key, Object value) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, jsonValue);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON for key: {}", key, e);
            throw new RuntimeException("Failed to save JSON data", e);
        }
    }

    @Override
    public void setJsonDataWithExpire(String key, Object value, long duration) {
        try {
            String jsonValue = objectMapper.writeValueAsString(value);
            Duration expireDuration = Duration.ofSeconds(duration);
            redisTemplate.opsForValue().set(key, jsonValue, expireDuration);
        } catch (Exception e) {
            log.error("Failed to serialize object to JSON for key: {}", key, e);
            throw new RuntimeException("Failed to save JSON data with expiration", e);
        }
    }

    @Override
    public <T> T getJsonData(String key, Class<T> clazz) {
        String jsonValue = redisTemplate.opsForValue().get(key);
        if (jsonValue == null) {
            return null;
        }
        
        try {
            return objectMapper.readValue(jsonValue, clazz);
        } catch (Exception e) {
            log.error("Failed to deserialize JSON for key: {}, value: {}", key, jsonValue, e);
            return null;
        }
    }

    @Override
    public <T> Map<String, T> multiGetJson(List<String> keys, Class<T> clazz) {
        if (keys == null || keys.isEmpty()) {
            return new HashMap<>();
        }
        
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        Map<String, T> result = new HashMap<>();
        
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String jsonValue = values != null && i < values.size() ? values.get(i) : null;
            
            if (jsonValue != null) {
                try {
                    T value = objectMapper.readValue(jsonValue, clazz);
                    result.put(key, value);
                } catch (Exception e) {
                    log.warn("Failed to deserialize JSON for key: {}, value: {}", key, jsonValue, e);
                    result.put(key, null);
                }
            }
        }
        
        return result;
    }
}
