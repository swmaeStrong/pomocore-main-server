package com.swmStrong.demo.infra.redis.repository;

import java.util.concurrent.TimeUnit;

public interface RedisRepository {
    String getData(String key);
    void setDataWithExpire(String key, String value, long duration);
    <T> void setData(String key, T value);
    void deleteData(String key);
    Long incrementWithExpireIfFirst(String key, long timeout, TimeUnit unit);
}
