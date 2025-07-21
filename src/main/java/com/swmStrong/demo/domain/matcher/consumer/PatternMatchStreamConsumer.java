package com.swmStrong.demo.domain.matcher.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.domain.matcher.core.PatternClassifier;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import com.swmStrong.demo.domain.usageLog.facade.UsageLogUpdateProvider;
import com.swmStrong.demo.infra.redis.stream.AbstractRedisStreamConsumer;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.LeaderBoardUsageMessage;
import com.swmStrong.demo.message.dto.PatternClassifyMessage;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PatternMatchStreamConsumer extends AbstractRedisStreamConsumer {

    private final ObjectMapper objectMapper;
    private final PatternClassifier patternClassifier;
    private final UsageLogUpdateProvider usageLogUpdateProvider;
    private final RedisStreamProducer redisStreamProducer;

    public PatternMatchStreamConsumer(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            PatternClassifier patternClassifier,
            UsageLogUpdateProvider usageLogUpdateProvider,
            RedisStreamProducer redisStreamProducer
    ) {
        super(stringRedisTemplate);
        this.objectMapper = objectMapper;
        this.patternClassifier = patternClassifier;
        this.usageLogUpdateProvider = usageLogUpdateProvider;
        this.redisStreamProducer = redisStreamProducer;
    }

    @Override
    protected void consume() {
        while (isRunning()) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        stringRedisTemplate.opsForStream().read(
                                Consumer.from(StreamConfig.PATTERN_MATCH.getGroup(), StreamConfig.PATTERN_MATCH.getConsumer()),
                                StreamReadOptions.empty().block(Duration.ofSeconds(2)).count(10),
                                StreamOffset.create(StreamConfig.PATTERN_MATCH.getStreamKey(),  ReadOffset.from(">"))
                        );


                for (MapRecord<String, Object, Object> record: records) {
                    Map<Object, Object> valueMap = record.getValue();
                    PatternClassifyMessage message = objectMapper.convertValue(valueMap, PatternClassifyMessage.class);

                    UsageLog usageLog = null;
                    if (message.categoryId().isEmpty()) {
                        PatternClassifier.ClassifiedResult result = patternClassifier.classify(message.app(), message.title(), message.url());
                        ObjectId usageLogId = new ObjectId(message.usageLogId());
                        usageLog = usageLogUpdateProvider.updateCategory(usageLogId, result.categoryPatternId());
                    } else {
                        usageLog = usageLogUpdateProvider.loadByUsageLogId(new ObjectId(message.usageLogId()));
                    }
                    stringRedisTemplate.opsForStream().acknowledge(StreamConfig.PATTERN_MATCH.getGroup(), record);

                    double duration = usageLog.getDuration();
                    if (message.margin() != null) {
                        duration = message.margin();
                    }

                    redisStreamProducer.send(
                    StreamConfig.LEADERBOARD.getStreamKey(),
                    LeaderBoardUsageMessage.builder()
                            .userId(usageLog.getUserId())
                            .categoryId(usageLog.getCategoryId())
                            .duration(duration)
                            .timestamp(usageLog.getTimestamp())
                            .build()
                    );
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
