package com.swmStrong.demo.config.redis;

import com.swmStrong.demo.infra.redis.StreamConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisStreamInitializer {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisStreamInitializer(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @PostConstruct
    public void initStreamGroup() {
        for (StreamConfig streamConfig : StreamConfig.values()) {
            try {
                stringRedisTemplate.opsForStream()
                        .createGroup(
                                streamConfig.getStreamKey(),
                                ReadOffset.latest(),
                                streamConfig.getGroup()
                        );
                log.info("그룹 생성 성공: {}", streamConfig.getGroup());
            } catch (RedisSystemException e) {
                if (e.getCause() instanceof io.lettuce.core.RedisBusyException) {
                    log.info("이미 존재하는 그룹입니다: {}", streamConfig.getGroup());
                } else {
                    log.error("그룹 생성 중 알 수 없는 예외 발생", e);
                    throw e;
                }
            }
        }
    }
}
