package com.swmStrong.demo.domain.leaderboard.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.domain.leaderboard.service.LeaderboardService;
import com.swmStrong.demo.infra.redis.stream.AbstractRedisStreamConsumer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.LeaderBoardUsageMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LeaderboardStreamConsumer extends AbstractRedisStreamConsumer {

    private final ObjectMapper objectMapper;
    private final LeaderboardService leaderboardService;

    public LeaderboardStreamConsumer(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            LeaderboardService leaderboardService
    ) {
        super(stringRedisTemplate);
        this.objectMapper = objectMapper;
        this.leaderboardService = leaderboardService;
    }

    @Override
    protected void consume() {
        while (isRunning()) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        stringRedisTemplate.opsForStream().read(
                                Consumer.from(StreamConfig.LEADERBOARD.getGroup(), StreamConfig.LEADERBOARD.getConsumer()),
                                StreamReadOptions.empty().block(Duration.ofSeconds(2)).count(10),
                                StreamOffset.create(StreamConfig.LEADERBOARD.getStreamKey(),  ReadOffset.from(">"))
                        );
                for (MapRecord<String, Object, Object> record: records) {
                    Map<Object, Object> valueMap = record.getValue();
                    LeaderBoardUsageMessage message = objectMapper.convertValue(valueMap, LeaderBoardUsageMessage.class);

                    leaderboardService.increaseScore(
                            message.categoryId(),
                            message.userId(),
                            message.duration(),
                            message.timestamp()
                    );

                    stringRedisTemplate.opsForStream().acknowledge(StreamConfig.LEADERBOARD.getGroup(), record);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                    e.printStackTrace();
                }
            }
        }
    }
}
