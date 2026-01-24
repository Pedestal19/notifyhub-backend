package com.notifyhub.worker.configuration;

import com.notifyhub.worker.service.InboundWorkHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkerConfig {

    @Bean
    InboundWorkHandler inboundWorkHandler() {
        return msg -> {
            // v1 stub: later route/enrich/call downstream
        };
    }
}
