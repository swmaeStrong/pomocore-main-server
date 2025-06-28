package com.swmStrong.demo.infra.redis.stream;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
            log.info("레디스 컨슈머 중지 시작: {}", this.getClass().getSimpleName());
            running = false;
            
            if (executorService != null) {
                executorService.shutdown();
                try {
                    if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                        log.warn("레디스 컨슈머 정상 종료 시간 초과, 강제 종료: {}", this.getClass().getSimpleName());
                        executorService.shutdownNow();
                        if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                            log.error("레디스 컨슈머 강제 종료 실패: {}", this.getClass().getSimpleName());
                        }
                    }
                } catch (InterruptedException e) {
                    log.warn("레디스 컨슈머 종료 중 인터럽트 발생: {}", this.getClass().getSimpleName());
                    executorService.shutdownNow();
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("레디스 컨슈머 종료 중 예외 발생: {}, 오류: {}", this.getClass().getSimpleName(), e.getMessage());
                }
            }
            
            log.info("레디스 컨슈머 중지 완료: {}", this.getClass().getSimpleName());
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    protected abstract void consume();
}
