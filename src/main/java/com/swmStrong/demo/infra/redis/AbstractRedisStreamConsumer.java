package com.swmStrong.demo.infra.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public abstract class AbstractRedisStreamConsumer implements SmartLifecycle {
    private volatile boolean running = false;
    private ExecutorService executorService;

    protected final StringRedisTemplate stringRedisTemplate;

    public AbstractRedisStreamConsumer(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void start() {
        if (!running) {
            running = true;
            executorService = Executors.newSingleThreadExecutor();
            executorService.submit(() -> {
                log.info("레디스 컨슈머 시작: {}", this.getClass().getSimpleName());
                consume();
            });
        }
    }

    @Override
    public void stop() {
        if (running) {
            running = false;
            executorService.shutdown();
            log.info("레디스 컨슈머 중지: {}", this.getClass().getSimpleName());
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    protected abstract void consume();
}
