package com.notifyhub.worker.service;

import com.notifyhub.worker.configuration.WorkerProperties;
import com.notifyhub.worker.inbound.db.InboundMessageEntity;
import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static com.notifyhub.worker.inbound.domain.InboundMessageStatus.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InboundMessageProcessorTest {

    @Mock
    InboundMessageRepository inboundMessageRepository;

    InboundMessageProcessor inboundMessageProcessor;

    @Mock
    InboundWorkHandler workHandler;

    private WorkerProperties props;

    @BeforeEach
    void setup() {
        props = new WorkerProperties(100, Duration.ofMinutes(2), 500L);
        inboundMessageProcessor = new InboundMessageProcessor(inboundMessageRepository, workHandler, props);
    }


    @Test
    void processBatch_movesReceivedToProcessed() {

        var m1 = InboundMessageEntity.builder()
                .status(RECEIVED)
                .updatedAt(OffsetDateTime.now().minusDays(1))
                .receivedAt(OffsetDateTime.now().minusDays(2))
                .createdAt(OffsetDateTime.now().minusDays(2))
                .channel("SMS").phoneNumber("+1").body("a")
                .build();

        when(inboundMessageRepository.findByStatusOrderByReceivedAtAsc(eq(RECEIVED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(m1)));
        when(inboundMessageRepository.findByStatusAndUpdatedAtBeforeOrderByReceivedAtAsc(
                eq(InboundMessageStatus.PROCESSING),
                any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        int processed = inboundMessageProcessor.processBatch(50);

        assertThat(processed).isEqualTo(1);
        assertThat(m1.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);
        assertThat(m1.getUpdatedAt()).isNotNull();

        verify(inboundMessageRepository)
                .findByStatusOrderByReceivedAtAsc(eq(RECEIVED), any(Pageable.class));

        verify(inboundMessageRepository, times(2)).saveAll(anyIterable());

        verifyNoMoreInteractions(inboundMessageRepository);
    }

    @Test
    void processBatch_returnsZero_whenNoMessages() {
        when(inboundMessageRepository.findByStatusOrderByReceivedAtAsc(eq(RECEIVED), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(inboundMessageRepository.findByStatusAndUpdatedAtBeforeOrderByReceivedAtAsc(
                eq(InboundMessageStatus.PROCESSING),
                any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        assertThat(inboundMessageProcessor.processBatch(50)).isEqualTo(0);

        verify(inboundMessageRepository).findByStatusOrderByReceivedAtAsc(eq(RECEIVED), any());
        verifyNoMoreInteractions(inboundMessageRepository);
    }

    @Test
    void processBatch_whenEmpty_returns0_andDoesNotSave() {
        when(inboundMessageRepository.findByStatusOrderByReceivedAtAsc(eq(InboundMessageStatus.RECEIVED), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(inboundMessageRepository.findByStatusAndUpdatedAtBeforeOrderByReceivedAtAsc(
                eq(InboundMessageStatus.PROCESSING),
                any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        int processed = inboundMessageProcessor.processBatch(50);

        assertThat(processed).isEqualTo(0);

        verify(inboundMessageRepository)
                .findByStatusOrderByReceivedAtAsc(eq(InboundMessageStatus.RECEIVED), any());

        verify(inboundMessageRepository, never()).saveAll(anyIterable());
        verifyNoMoreInteractions(inboundMessageRepository);
    }

    @Test
    void processBatch_whenWorkThrows_marksFailed_andContinues() {
        var ok = InboundMessageEntity.builder()
                .status(InboundMessageStatus.RECEIVED)
                .channel("SMS").phoneNumber("+1").body("ok")
                .receivedAt(OffsetDateTime.now().minusMinutes(2))
                .createdAt(OffsetDateTime.now().minusMinutes(2))
                .updatedAt(OffsetDateTime.now().minusMinutes(2))
                .build();

        var bad = InboundMessageEntity.builder()
                .status(InboundMessageStatus.RECEIVED)
                .channel("SMS").phoneNumber("+2").body("bad")
                .receivedAt(OffsetDateTime.now().minusMinutes(1))
                .createdAt(OffsetDateTime.now().minusMinutes(1))
                .updatedAt(OffsetDateTime.now().minusMinutes(1))
                .build();

        when(inboundMessageRepository.findByStatusOrderByReceivedAtAsc(eq(InboundMessageStatus.RECEIVED), any()))
                .thenReturn(new PageImpl<>(List.of(ok, bad)));

        doNothing().when(workHandler).handle(any(InboundMessageEntity.class));
        doThrow(new RuntimeException("boom"))
                .when(workHandler)
                .handle(argThat(m -> "bad".equals(m.getBody())));
        when(inboundMessageRepository.findByStatusAndUpdatedAtBeforeOrderByReceivedAtAsc(
                eq(InboundMessageStatus.PROCESSING),
                any(),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of()));

        int count = inboundMessageProcessor.processBatch(50);

        assertThat(count).isEqualTo(2);
        assertThat(ok.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);
        assertThat(bad.getStatus()).isEqualTo(InboundMessageStatus.FAILED);

        verify(inboundMessageRepository)
                .findByStatusOrderByReceivedAtAsc(eq(InboundMessageStatus.RECEIVED), any());
        verify(inboundMessageRepository, times(2)).saveAll(anyIterable());

        verify(workHandler, times(2)).handle(any(InboundMessageEntity.class));
    }

    @Test
    void processBatch_whenReceivedEmpty_butStuckProcessingExists_processesStuck() {
        var stuck = InboundMessageEntity.builder()
                .status(InboundMessageStatus.PROCESSING)
                .channel("SMS").phoneNumber("+2").body("stuck")
                .receivedAt(OffsetDateTime.now().minusMinutes(10))
                .createdAt(OffsetDateTime.now().minusMinutes(10))
                .updatedAt(OffsetDateTime.now().minusMinutes(10))
                .build();

        when(inboundMessageRepository.findByStatusOrderByReceivedAtAsc(eq(RECEIVED), any()))
                .thenReturn(new PageImpl<>(List.of()));

        when(inboundMessageRepository.findByStatusAndUpdatedAtBeforeOrderByReceivedAtAsc(
                eq(InboundMessageStatus.PROCESSING), any(), any()
        )).thenReturn(new PageImpl<>(List.of(stuck)));

        doNothing().when(workHandler).handle(any(InboundMessageEntity.class));

        int count = inboundMessageProcessor.processBatch(50);

        assertThat(count).isEqualTo(1);
        assertThat(stuck.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);

        verify(inboundMessageRepository).findByStatusOrderByReceivedAtAsc(eq(RECEIVED), any());
        verify(inboundMessageRepository).findByStatusAndUpdatedAtBeforeOrderByReceivedAtAsc(eq(InboundMessageStatus.PROCESSING), any(), any());
        verify(inboundMessageRepository, times(2)).saveAll(anyIterable());
    }

    @Test
    void processBatch_neverProcessesMoreThanLimit() {
        var now = OffsetDateTime.now();

        var r1 = InboundMessageEntity.builder().status(RECEIVED).channel("SMS").phoneNumber("+1").body("r1")
                .receivedAt(now.minusMinutes(3)).createdAt(now.minusMinutes(3)).updatedAt(now.minusMinutes(3)).build();

        var r2 = InboundMessageEntity.builder().status(RECEIVED).channel("SMS").phoneNumber("+2").body("r2")
                .receivedAt(now.minusMinutes(2)).createdAt(now.minusMinutes(2)).updatedAt(now.minusMinutes(2)).build();

        var stuck = InboundMessageEntity.builder().status(InboundMessageStatus.PROCESSING).channel("SMS").phoneNumber("+3").body("stuck")
                .receivedAt(now.minusMinutes(10)).createdAt(now.minusMinutes(10)).updatedAt(now.minusMinutes(10)).build();

        when(inboundMessageRepository.findByStatusOrderByReceivedAtAsc(eq(RECEIVED), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(r1, r2)));

        doNothing().when(workHandler).handle(any(InboundMessageEntity.class));

        int count = inboundMessageProcessor.processBatch(2);

        assertThat(count).isEqualTo(2);

        verify(inboundMessageRepository, never())
                .findByStatusAndUpdatedAtBeforeOrderByReceivedAtAsc(eq(InboundMessageStatus.PROCESSING), any(), any());
    }
}
