package com.swmStrong.demo.config;

import io.sentry.Sentry;
import io.sentry.spring.jakarta.EnableSentry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

@EnableSentry(dsn = "${sentry.dsn:}")
@Configuration
@Profile("!test")
public class SentryConfig {

    @Value("${sentry.environment:development}")
    private String environment;

    @Value("${sentry.traces-sample-rate:0.1}")
    private Double tracesSampleRate;

    @Value("${sentry.profiles-sample-rate:0.1}")
    private Double profilesSampleRate;

    @PostConstruct
    public void init() {
        Sentry.configureScope(scope -> {
            scope.setTag("environment", environment);
        });
    }
}