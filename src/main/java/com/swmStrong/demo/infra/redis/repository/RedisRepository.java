package com.swmStrong.demo.infra.redis.repository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface RedisRepository {
    String getData(String key);
    Map<String, String> multiGet(List<String> keys);
    <T> void setDataWithExpire(String key, T value, long duration);
    <T> void setData(String key, T value);
    void deleteData(String key);
    Long incrementWithExpireIfFirst(String key, long timeout, TimeUnit unit);
    Set<String> findKeys(String regex);
    
    // JSON specific methods
    void setJsonData(String key, Object value);
    void setJsonDataWithExpire(String key, Object value, long duration);
    <T> T getJsonData(String key, Class<T> clazz);
    <T> Map<String, T> multiGetJson(List<String> keys, Class<T> clazz);
}
