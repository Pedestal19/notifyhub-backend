package com.notifyhub.worker.service;

import com.notifyhub.worker.inbound.db.InboundMessageEntity;
import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;

@Testcontainers
@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
@ActiveProfiles("test")
public class InboundMessageProcessorIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        r.add("notifyhub.worker.max-page-size", () -> "100");
        r.add("notifyhub.worker.retry-after", () -> "PT2M");
        r.add("notifyhub.worker.poll-delay-ms", () -> "500");
        r.add("notifyhub.worker.batch-size", () -> "50");
        r.add("notifyhub.worker.health-stale-after", () -> "PT60S");
    }

    @Autowired
    InboundMessageRepository inboundMessageRepository;
    @Autowired
    InboundMessageProcessor inboundMessageProcessor;
    @MockitoBean
    InboundWorkHandler inboundWorkHandler;

    @BeforeEach
    void clean() {
        inboundMessageRepository.deleteAll();
    }

    @Test
    void processor_updatesDbStatuses() {
        doNothing().when(inboundWorkHandler).handle(org.mockito.ArgumentMatchers.any());

        var msg = InboundMessageEntity.builder()
                .channel("SMS")
                .phoneNumber("+234...")
                .body("hello")
                .status(InboundMessageStatus.RECEIVED)
                .receivedAt(OffsetDateTime.now().minusMinutes(1))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        var saved = inboundMessageRepository.save(msg);

        BatchResult result = inboundMessageProcessor.processBatch(50);
        assertThat(result.claimed()).isEqualTo(1);
        assertThat(result.processed()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(0);

        var reloaded = inboundMessageRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);
    }
}
