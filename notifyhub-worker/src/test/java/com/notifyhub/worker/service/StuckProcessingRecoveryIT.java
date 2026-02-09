package com.notifyhub.worker.service;

import com.notifyhub.worker.inbound.db.InboundMessageEntity;
import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import com.notifyhub.worker.support.TestWorkerProps;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@Testcontainers
@SpringBootTest(properties = "spring.task.scheduling.enabled=false")
@ActiveProfiles("test")
class StuckProcessingRecoveryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);

        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        TestWorkerProps.addWorkerProps(r);
    }

    @Autowired InboundMessageRepository repo;
    @Autowired InboundMessageProcessor processor;

    @MockitoBean InboundWorkHandler handler;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    void stuckProcessing_isReclaimedAfterRetryAfter_andProcessed() {
        doNothing().when(handler).handle(any());

        var stuck = InboundMessageEntity.builder()
                .channel("SMS")
                .phoneNumber("+234...")
                .body("stuck")
                .status(InboundMessageStatus.PROCESSING)
                .receivedAt(OffsetDateTime.now().minusMinutes(10))
                .createdAt(OffsetDateTime.now().minusMinutes(10))
                .updatedAt(OffsetDateTime.now().minusMinutes(5))
                .build();

        var saved = repo.save(stuck);

        BatchResult result = processor.processBatch(50);

        assertThat(result.claimed()).isEqualTo(1);
        assertThat(result.processed()).isEqualTo(1);
        assertThat(result.failed()).isEqualTo(0);

        var reloaded = repo.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);
    }
}
