package com.notifyhub.worker.service;

import com.notifyhub.worker.configuration.WorkerProperties;
import com.notifyhub.worker.inbound.db.InboundMessageEntity;
import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static com.notifyhub.worker.inbound.domain.InboundMessageStatus.RECEIVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InboundMessageProcessorTest {

    @Mock
    InboundMessageRepository inboundMessageRepository;
    InboundMessageProcessor inboundMessageProcessor;
    @Mock
    InboundWorkHandler workHandler;
    @Mock InboundMessageClaimer claimer;


    private WorkerProperties props;

    @BeforeEach
    void setup() {
        props = new WorkerProperties(100, Duration.ofMinutes(2), 500L);
        inboundMessageProcessor = new InboundMessageProcessor(inboundMessageRepository, workHandler, props, claimer);
    }


    @Test
    void processBatch_movesClaimedReceivedToProcessed() {
        var m1 = InboundMessageEntity.builder()
                .status(RECEIVED)
                .updatedAt(OffsetDateTime.now().minusDays(1))
                .receivedAt(OffsetDateTime.now().minusDays(2))
                .createdAt(OffsetDateTime.now().minusDays(2))
                .channel("SMS").phoneNumber("+1").body("a")
                .build();

        // Step 8: processor calls claimer, not repo queries
        when(claimer.claimReceived(50)).thenReturn(List.of(m1));

        doNothing().when(workHandler).handle(any(InboundMessageEntity.class));

        int claimed = inboundMessageProcessor.processBatch(50);

        assertThat(claimed).isEqualTo(1);
        assertThat(m1.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);
        assertThat(m1.getUpdatedAt()).isNotNull();

        verify(claimer).claimReceived(50);
        verify(claimer, never()).claimStuckProcessing(anyInt());

        verify(workHandler).handle(m1);
        verify(inboundMessageRepository).saveAll(anyIterable());
        verifyNoMoreInteractions(inboundMessageRepository);
    }

    @Test
    void processBatch_returnsZero_whenNoMessagesClaimed() {
        when(claimer.claimReceived(50)).thenReturn(List.of());
        when(claimer.claimStuckProcessing(50)).thenReturn(List.of());

        int claimed = inboundMessageProcessor.processBatch(50);

        assertThat(claimed).isEqualTo(0);

        verify(claimer).claimReceived(50);
        verify(claimer).claimStuckProcessing(50);

        verifyNoInteractions(workHandler);
        verify(inboundMessageRepository, never()).saveAll(anyIterable());
        verifyNoMoreInteractions(inboundMessageRepository);
    }

    @Test
    void processBatch_whenReceivedEmpty_butStuckClaimed_processesStuck() {
        var stuck = InboundMessageEntity.builder()
                .status(InboundMessageStatus.PROCESSING)
                .channel("SMS").phoneNumber("+2").body("stuck")
                .receivedAt(OffsetDateTime.now().minusMinutes(10))
                .createdAt(OffsetDateTime.now().minusMinutes(10))
                .updatedAt(OffsetDateTime.now().minusMinutes(10))
                .build();

        when(claimer.claimReceived(50)).thenReturn(List.of());
        when(claimer.claimStuckProcessing(50)).thenReturn(List.of(stuck));

        doNothing().when(workHandler).handle(any(InboundMessageEntity.class));

        int claimed = inboundMessageProcessor.processBatch(50);

        assertThat(claimed).isEqualTo(1);
        assertThat(stuck.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);

        verify(claimer).claimReceived(50);
        verify(claimer).claimStuckProcessing(50);
        verify(workHandler).handle(stuck);
        verify(inboundMessageRepository).saveAll(anyIterable());
    }

    @Test
    void processBatch_whenWorkThrows_marksFailed_andContinues() {
        var ok = InboundMessageEntity.builder()
                .status(RECEIVED)
                .channel("SMS").phoneNumber("+1").body("ok")
                .receivedAt(OffsetDateTime.now().minusMinutes(2))
                .createdAt(OffsetDateTime.now().minusMinutes(2))
                .updatedAt(OffsetDateTime.now().minusMinutes(2))
                .build();

        var bad = InboundMessageEntity.builder()
                .status(RECEIVED)
                .channel("SMS").phoneNumber("+2").body("bad")
                .receivedAt(OffsetDateTime.now().minusMinutes(1))
                .createdAt(OffsetDateTime.now().minusMinutes(1))
                .updatedAt(OffsetDateTime.now().minusMinutes(1))
                .build();

        when(claimer.claimReceived(50)).thenReturn(List.of(ok, bad));

        doNothing().when(workHandler).handle(any(InboundMessageEntity.class));
        doThrow(new RuntimeException("boom"))
                .when(workHandler).handle(argThat(m -> "bad".equals(m.getBody())));

        int claimed = inboundMessageProcessor.processBatch(50);

        assertThat(claimed).isEqualTo(2);
        assertThat(ok.getStatus()).isEqualTo(InboundMessageStatus.PROCESSED);
        assertThat(bad.getStatus()).isEqualTo(InboundMessageStatus.FAILED);

        verify(workHandler, times(2)).handle(any(InboundMessageEntity.class));
        verify(inboundMessageRepository).saveAll(anyIterable());
    }

    @Test
    void processBatch_neverClaimsMoreThanLimit() {
        // Explain-like-I'm-5:
        // If you pass limit=2, processor must ask the claimer for 2 (not 100).
        when(claimer.claimReceived(2)).thenReturn(List.of(
                InboundMessageEntity.builder().status(RECEIVED).body("r1").channel("SMS").phoneNumber("+1")
                        .receivedAt(OffsetDateTime.now()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build(),
                InboundMessageEntity.builder().status(RECEIVED).body("r2").channel("SMS").phoneNumber("+2")
                        .receivedAt(OffsetDateTime.now()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build()
        ));

        doNothing().when(workHandler).handle(any(InboundMessageEntity.class));

        int claimed = inboundMessageProcessor.processBatch(2);

        assertThat(claimed).isEqualTo(2);
        verify(claimer).claimReceived(2);
        verify(claimer, never()).claimStuckProcessing(anyInt());
    }
}
