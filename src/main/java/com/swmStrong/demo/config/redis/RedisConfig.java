package com.swmStrong.demo.config.redis;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PreDestroy;
import java.time.Duration;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    private ClientResources clientResources;
    private LettuceConnectionFactory connectionFactory;

    @Bean
    public ClientResources clientResources() {
        this.clientResources = DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();
        return this.clientResources;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory(ClientResources clientResources) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        
        ClientOptions clientOptions = ClientOptions.builder()
                .timeoutOptions(TimeoutOptions.builder()
                        .fixedTimeout(Duration.ofSeconds(3))
                        .build())
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build();

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .clientOptions(clientOptions)
                .clientResources(clientResources)
                .commandTimeout(Duration.ofSeconds(3))
                .shutdownTimeout(Duration.ofSeconds(5))
                .build();

        this.connectionFactory = new LettuceConnectionFactory(config, clientConfig);
        this.connectionFactory.setShareNativeConnection(false);
        this.connectionFactory.setValidateConnection(true);
        
        return this.connectionFactory;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setEnableTransactionSupport(false);
        return template;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.setEnableTransactionSupport(false);
        template.afterPropertiesSet();
        
        return template;
    }

    @PreDestroy
    public void cleanup() {
        log.info("Redis graceful shutdown 시작");
        
        try {
            if (connectionFactory != null) {
                log.info("Redis connection factory 종료 중...");
                connectionFactory.destroy();
                log.info("Redis connection factory 종료 완료");
            }
        } catch (Exception e) {
            log.warn("Redis connection factory 종료 중 예외 발생: {}", e.getMessage());
        }

        try {
            if (clientResources != null && !clientResources.shutdown().isDone()) {
                log.info("Redis client resources 종료 중...");
                clientResources.shutdown(3, 5, java.util.concurrent.TimeUnit.SECONDS);
                log.info("Redis client resources 종료 완료");
            }
        } catch (Exception e) {
            log.warn("Redis client resources 종료 중 예외 발생: {}", e.getMessage());
        }
        
        log.info("Redis graceful shutdown 완료");
    }
}