package com.swmStrong.demo.domain.sessionScore.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.domain.sessionScore.service.SessionScoreService;
import com.swmStrong.demo.infra.redis.stream.AbstractRedisStreamConsumer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.SessionScoreMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SessionScoreStreamConsumer extends AbstractRedisStreamConsumer {

    private final ObjectMapper objectMapper;
    private final SessionScoreService sessionScoreService;

    public SessionScoreStreamConsumer(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            SessionScoreService sessionScoreService
    ) {
        super(stringRedisTemplate);
        this.objectMapper = objectMapper;
        this.sessionScoreService = sessionScoreService;
    }

    @Override
    protected void consume() {
        while (isRunning()) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        stringRedisTemplate.opsForStream().read(
                                Consumer.from(StreamConfig.SESSION_SCORE_SAVE.getGroup(), StreamConfig.SESSION_SCORE_SAVE.getConsumer()),
                                StreamReadOptions.empty().block(Duration.ofSeconds(2)).count(10),
                                StreamOffset.create(StreamConfig.SESSION_SCORE_SAVE.getStreamKey(), ReadOffset.from(">"))
                        );

                for (MapRecord<String, Object, Object> record : records) {
                    try {
                        Map<Object, Object> valueMap = record.getValue();
                        SessionScoreMessage message = objectMapper.convertValue(valueMap, SessionScoreMessage.class);

                        log.info("Session score message received: userId={}, session={}, sessionDate={}",
                                message.userId(), message.session(), message.sessionDate());

                        // Call the session score processing method (previously handleSessionEnded)
                        sessionScoreService.processSessionEnded(message.userId(), message.session(), message.sessionDate());

                        // Acknowledge the message
                        stringRedisTemplate.opsForStream().acknowledge(StreamConfig.SESSION_SCORE_SAVE.getGroup(), record);

                        log.info("Session score message processed successfully: userId={}, session={}, sessionDate={}",
                                message.userId(), message.session(), message.sessionDate());

                    } catch (Exception e) {
                        log.error("Error processing session score message: {}", record.getValue(), e);
                        // Still acknowledge to prevent infinite retry
                        stringRedisTemplate.opsForStream().acknowledge(StreamConfig.SESSION_SCORE_SAVE.getGroup(), record);
                    }
                }
            } catch (Exception e) {
                log.error("Error reading from session score stream: {}", e.getMessage(), e);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}