package com.notifyhub.worker.scheduler;

import com.notifyhub.worker.configuration.WorkerProperties;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class WorkerObservabilityConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public InboundWorkerStats inboundWorkerStats(Clock clock) {
        return new InboundWorkerStats(clock);
    }

    @Bean
    public HealthIndicator workerHealthIndicator(InboundWorkerStats stats, WorkerProperties props, Clock clock) {
        return new WorkerHealthIndicator(stats, props, clock);
    }

    @Bean
    public WorkerMetrics workerMetrics(InboundWorkerStats stats) {
        return new WorkerMetrics(stats);
    }
}
