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
}
