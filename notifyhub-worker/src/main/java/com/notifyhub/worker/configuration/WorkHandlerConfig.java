package com.notifyhub.worker.configuration;

import com.notifyhub.worker.scheduler.InboundWorkerStats;
import com.notifyhub.worker.scheduler.WorkerHealthIndicator;
import com.notifyhub.worker.scheduler.WorkerMetrics;
import com.notifyhub.worker.service.InboundWorkHandler;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class WorkHandlerConfig {

    @Bean
    InboundWorkHandler inboundWorkHandler() {
        return msg -> {
            // v1 stub: later route/enrich/call downstream
        };
    }
}
