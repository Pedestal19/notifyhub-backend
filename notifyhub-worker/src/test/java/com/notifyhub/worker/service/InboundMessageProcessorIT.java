package com.notifyhub.worker.service;

import com.notifyhub.worker.inbound.db.InboundMessageEntity;
import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import com.notifyhub.worker.service.InboundMessageProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
public class InboundMessageProcessorIT {

    @Autowired
    InboundMessageRepository inboundMessageRepository;
    @Autowired
    InboundMessageProcessor inboundMessageProcessor;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clean() { jdbcTemplate.execute("truncate table inbound_message"); }

    @Test
    void processor_updatesDbStatuses() {
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

        inboundMessageProcessor.processBatch(50);

        var reloaded = inboundMessageRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);
    }
}
