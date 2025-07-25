package com.swmStrong.demo.domain.matcher.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.domain.matcher.core.PatternClassifier;
import com.swmStrong.demo.domain.pomodoro.facade.PomodoroUpdateProvider;
import com.swmStrong.demo.infra.redis.stream.AbstractRedisStreamConsumer;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.LeaderBoardUsageMessage;
import com.swmStrong.demo.message.dto.PomodoroPatternClassifyMessage;
import com.swmStrong.demo.message.event.SessionEndedEvent;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PomodoroPatternMatchStreamConsumer extends AbstractRedisStreamConsumer {

    private final ObjectMapper objectMapper;
    private final PatternClassifier patternClassifier;
    private final PomodoroUpdateProvider pomodoroUpdateProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final RedisStreamProducer redisStreamProducer;

    public PomodoroPatternMatchStreamConsumer(
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper,
            PatternClassifier patternClassifier,
            PomodoroUpdateProvider pomodoroUpdateProvider,
            ApplicationEventPublisher eventPublisher,
            RedisStreamProducer redisStreamProducer
    ) {
        super(stringRedisTemplate);
        this.objectMapper = objectMapper;
        this.patternClassifier = patternClassifier;
        this.pomodoroUpdateProvider = pomodoroUpdateProvider;
        this.eventPublisher = eventPublisher;
        this.redisStreamProducer = redisStreamProducer;
    }
    // TODO : 개수 늘리는 법 찾아 보기
    @Override
    protected void consume() {
        while (isRunning()) {
            try {
                List<MapRecord<String, Object, Object>> records =
                        stringRedisTemplate.opsForStream().read(
                                Consumer.from(StreamConfig.POMODORO_PATTERN_MATCH.getGroup(), StreamConfig.POMODORO_PATTERN_MATCH.getConsumer()),
                                StreamReadOptions.empty().block(Duration.ofSeconds(2)).count(10),
                                StreamOffset.create(StreamConfig.POMODORO_PATTERN_MATCH.getStreamKey(),  ReadOffset.from(">"))
                        );

                for (MapRecord<String, Object, Object> record: records) {
                    Map<Object, Object> valueMap = record.getValue();
                    PomodoroPatternClassifyMessage message = objectMapper.convertValue(valueMap, PomodoroPatternClassifyMessage.class);

                    PatternClassifier.ClassifiedResult result = patternClassifier.classify(message.app(), message.title(), message.url());
                    pomodoroUpdateProvider.updatePomodoroUsageLogByCategoryId(new ObjectId(message.pomodoroUsageLogId()), result.categoryPatternId());
                    pomodoroUpdateProvider.updateCategorizedDataByCategoryId(new ObjectId(message.categorizedDataId()), result.categoryPatternId(), result.isLLMBased());

                    if (message.isEnd()) {
                        eventPublisher.publishEvent(SessionEndedEvent.builder()
                                .userId(message.userId())
                                .session(message.session())
                                .sessionDate(message.sessionDate())
                                .build()
                        );
                    }

                    redisStreamProducer.send(
                            StreamConfig.LEADERBOARD.getStreamKey(),
                            LeaderBoardUsageMessage.builder()
                                    .userId(message.userId())
                                    .categoryId(result.categoryPatternId())
                                    .duration(message.duration())
                                    .timestamp(message.timestamp())
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
