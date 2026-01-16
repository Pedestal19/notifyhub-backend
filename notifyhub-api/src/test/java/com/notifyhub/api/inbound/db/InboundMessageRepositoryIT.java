package com.notifyhub.api.inbound.db;

import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class InboundMessageRepositoryIT {

    @Autowired
    InboundMessageRepository repository;

    @Test
    void findAllByOrderByReceivedAtDesc_returnsNewestFirst() {
        var older = InboundMessageEntity.builder()
                .channel("SMS")
                .phoneNumber("+2348000000001")
                .body("old")
                .status(InboundMessageStatus.RECEIVED)
                .receivedAt(OffsetDateTime.parse("2026-01-01T10:00:00Z"))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        var newer = InboundMessageEntity.builder()
                .channel("SMS")
                .phoneNumber("+2348000000002")
                .body("new")
                .status(InboundMessageStatus.RECEIVED)
                .receivedAt(OffsetDateTime.parse("2026-01-02T10:00:00Z"))
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        repository.save(older);
        repository.save(newer);

        var page = repository.findAllByOrderByReceivedAtDesc(PageRequest.of(0, 10));

        assertThat(page.getContent()).hasSizeGreaterThanOrEqualTo(2);
        assertThat(page.getContent().get(0).getBody()).isEqualTo("new");
        assertThat(page.getContent().get(1).getBody()).isEqualTo("old");
    }
}
