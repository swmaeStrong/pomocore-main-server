package com.swmStrong.demo.infra.redis.repository;

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

@Component
public class RedisRepositoryImpl implements RedisRepository {
    private final StringRedisTemplate redisTemplate;

    public RedisRepositoryImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
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

    public void setDataWithExpire(String key, String value, long duration) {
        Duration expireDuration = Duration.ofSeconds(duration);
        redisTemplate.opsForValue().set(key, value, expireDuration);
    }

    public <T> void setData(String key, T value) {
        redisTemplate.opsForValue().set(key, value.toString());
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public Long incrementWithExpireIfFirst(String key, long timeout, TimeUnit unit) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, timeout, unit);
        }
        return count;
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
}
