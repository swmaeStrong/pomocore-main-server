package com.swmStrong.demo.infra.redis.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RedisRepositoryImpl implements RedisRepository {
    private final StringRedisTemplate redisTemplate;

    public RedisRepositoryImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
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
}
