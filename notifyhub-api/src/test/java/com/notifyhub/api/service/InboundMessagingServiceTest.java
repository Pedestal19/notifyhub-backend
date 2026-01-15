package com.notifyhub.api.service;

import com.notifyhub.api.dto.InboundMessageIngestRequest;
import com.notifyhub.api.dto.InboundMessageResponse;
import com.notifyhub.api.inbound.db.InboundMessageEntity;
import com.notifyhub.api.inbound.db.InboundMessageRepository;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class InboundMessagingServiceTest {

    @Mock
    InboundMessageRepository inboundMessageRepository;

    @InjectMocks
    InboundMessageService inboundMessageService;

    @Test
    void ingest_defaultsReceivedAt_whenNotProvided(){

        InboundMessageIngestRequest inboundMessageIngestRequest = new InboundMessageIngestRequest(
                "SMS",
                "+2348012345678",
                "hello",
                null
        );

        InboundMessageResponse inboundMessageResponse = inboundMessageService.ingest(inboundMessageIngestRequest);

        assertThat(inboundMessageResponse.status()).isEqualTo(InboundMessageStatus.RECEIVED);
        assertThat(inboundMessageResponse.receivedAt()).isNotNull();

        ArgumentCaptor<InboundMessageEntity> captor = ArgumentCaptor.forClass(InboundMessageEntity.class);
        verify(inboundMessageRepository).save(captor.capture());

        InboundMessageEntity saved = captor.getValue();
        assertThat(saved.getChannel()).isEqualTo("SMS");
        assertThat(saved.getPhoneNumber()).isEqualTo("+2348012345678");
        assertThat(saved.getBody()).isEqualTo("hello");
        assertThat(saved.getStatus()).isEqualTo(InboundMessageStatus.RECEIVED);
        assertThat(saved.getReceivedAt()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void ingest_usesProvidedReceivedAt_whenPresent(){
        OffsetDateTime provided = OffsetDateTime.parse("2026-01-01T10:00:00+01:00");
        InboundMessageIngestRequest inboundMessageIngestRequest = new InboundMessageIngestRequest(
                "SMS",
                "+2348012345678",
                "hello",
                provided
        );

        InboundMessageResponse inboundMessageResponse = inboundMessageService.ingest(inboundMessageIngestRequest);

        assertThat(inboundMessageResponse.receivedAt()).isEqualTo(provided);

        ArgumentCaptor<InboundMessageEntity> captor = ArgumentCaptor.forClass(InboundMessageEntity.class);
        verify(inboundMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getReceivedAt()).isEqualTo(provided);
    }
}
