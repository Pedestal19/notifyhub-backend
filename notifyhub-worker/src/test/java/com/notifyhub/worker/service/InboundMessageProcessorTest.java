package com.notifyhub.worker.service;

import com.notifyhub.worker.inbound.db.InboundMessageEntity;
import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InboundMessageProcessorTest {

    @Mock
    InboundMessageRepository inboundMessageRepository;

    @InjectMocks
    InboundMessageProcessor inboundMessageProcessor;

    @Test
    void processBatch_movesReceivedToProcessed() {

        var m1 = InboundMessageEntity.builder()
                .status(InboundMessageStatus.RECEIVED)
                .updatedAt(OffsetDateTime.now().minusDays(1))
                .receivedAt(OffsetDateTime.now().minusDays(2))
                .createdAt(OffsetDateTime.now().minusDays(2))
                .channel("SMS").phoneNumber("+1").body("a")
                .build();

        when(inboundMessageRepository.findByStatusOrderByReceivedAtAsc(eq(InboundMessageStatus.RECEIVED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(m1)));

        int processed = inboundMessageProcessor.processBatch(50);

        assertThat(processed).isEqualTo(1);
        assertThat(m1.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);
        assertThat(m1.getUpdatedAt()).isNotNull();

        verify(inboundMessageRepository)
                .findByStatusOrderByReceivedAtAsc(eq(InboundMessageStatus.RECEIVED), any(Pageable.class));

        verify(inboundMessageRepository, times(2)).saveAll(anyIterable());

        verifyNoMoreInteractions(inboundMessageRepository);
    }
}
