package com.notifyhub.worker.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "notifyhub.worker")
public record WorkerProperties(
        int maxPageSize,
        Duration retryAfter,
        long pollDelayMs
) {
}
