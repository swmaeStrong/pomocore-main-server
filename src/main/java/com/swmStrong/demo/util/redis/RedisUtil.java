package com.swmStrong.demo.util.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;

    public RedisUtil(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public static final String REDIS_REFRESH_TOKEN_PREFIX = "auth:refreshToken:";

    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setDataWithExpire(String key, String value, long duration) {
        Duration expireDuration = Duration.ofSeconds(duration);
        redisTemplate.opsForValue().set(key, value, expireDuration);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}
