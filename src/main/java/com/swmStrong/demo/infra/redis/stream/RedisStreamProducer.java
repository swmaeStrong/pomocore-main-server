package com.swmStrong.demo.infra.redis.stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RedisStreamProducer {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
    
    // 백업 큐: Redis 연결 실패 시 메시지 임시 저장
    private final ConcurrentLinkedQueue<QueuedMessage> backupQueue = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService retryScheduler = Executors.newScheduledThreadPool(1);
    private static final int MAX_QUEUE_SIZE = 10000; // 최대 큐 크기
    private static final long RETRY_INTERVAL_SECONDS = 30; // 재시도 간격

    public RedisStreamProducer(ObjectMapper objectMapper, StringRedisTemplate stringRedisTemplate) {
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
        
        // Redis 연결 복구 재시도 스케줄러 시작
        retryScheduler.scheduleAtFixedRate(this::retryFailedMessages, 
                RETRY_INTERVAL_SECONDS, RETRY_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public <T> void send(String streamKey, T dto) {
        try {
            Map<String, String> body = objectMapper.convertValue(dto, new TypeReference<>() {});
            stringRedisTemplate.opsForStream()
                    .add(MapRecord.create(streamKey, body));
        } catch (Exception e) {
            log.warn("Redis connection failed, queuing message for retry: streamKey={}, error={}", 
                    streamKey, e.getMessage());
            queueMessageForRetry(streamKey, Collections.singletonList(dto));
        }
    }

    public <T> void sendBatch(String streamKey, List<T> dtos) {
        try {
            stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (T dto : dtos) {
                    Map<String, String> body = objectMapper.convertValue(dto, new TypeReference<>() {});
                    stringRedisTemplate.opsForStream().add(MapRecord.create(streamKey, body));
                }
                return null;
            });
        } catch (Exception e) {
            log.warn("Redis connection failed, queuing batch messages for retry: streamKey={}, size={}, error={}", 
                    streamKey, dtos.size(), e.getMessage());
            queueMessageForRetry(streamKey, dtos);
        }
    }
    
    /**
     * Redis 연결 실패 시 메시지를 백업 큐에 저장
     */
    private <T> void queueMessageForRetry(String streamKey, List<T> dtos) {
        // 큐 크기 제한 확인
        if (backupQueue.size() + dtos.size() > MAX_QUEUE_SIZE) {
            log.error("Backup queue is full, dropping {} messages for streamKey={}", dtos.size(), streamKey);
            return;
        }
        
        long timestamp = System.currentTimeMillis();
        for (T dto : dtos) {
            try {
                Map<String, String> body = objectMapper.convertValue(dto, new TypeReference<>() {});
                backupQueue.offer(new QueuedMessage(streamKey, body, timestamp));
            } catch (Exception e) {
                log.error("Failed to serialize message for backup queue: {}", e.getMessage());
            }
        }
        
        log.warn("Queued {} messages for retry, total queue size: {}", dtos.size(), backupQueue.size());
    }
    
    /**
     * 백업 큐의 메시지들을 Redis에 재전송 시도
     */
    private void retryFailedMessages() {
        if (backupQueue.isEmpty()) {
            return;
        }
        
        log.warn("Attempting to retry {} queued messages", backupQueue.size());
        
        List<QueuedMessage> processedMessages = new ArrayList<>();
        int retryCount = 0;
        int successCount = 0;
        
        // 최대 1000개씩 처리하여 메모리 효율성 확보
        while (!backupQueue.isEmpty() && retryCount < 1000) {
            QueuedMessage queuedMessage = backupQueue.poll();
            if (queuedMessage == null) break;
            
            retryCount++;
            
            // TTL 확인 (30분 이상 된 메시지는 버림)
            if (System.currentTimeMillis() - queuedMessage.timestamp() > 30 * 60 * 1000) {
                log.warn("Dropping expired message: streamKey={}, age={}ms", 
                        queuedMessage.streamKey(), System.currentTimeMillis() - queuedMessage.timestamp());
                continue;
            }
            
            try {
                stringRedisTemplate.opsForStream()
                        .add(MapRecord.create(queuedMessage.streamKey(), queuedMessage.body()));
                successCount++;
            } catch (Exception e) {
                // 여전히 실패하면 다시 큐에 넣음
                processedMessages.add(queuedMessage);
            }
        }
        
        // 실패한 메시지들을 큐 앞쪽으로 다시 추가
        for (int i = processedMessages.size() - 1; i >= 0; i--) {
            backupQueue.offer(processedMessages.get(i));
        }
        
        if (retryCount > 0) {
            log.warn("Retry completed: attempted={}, succeeded={}, failed={}, remaining={}",
                    retryCount, successCount, processedMessages.size(), backupQueue.size());
        }
    }

    private record QueuedMessage(
            String streamKey,
            Map<String, String> body,
            long timestamp
    ) {}
}
