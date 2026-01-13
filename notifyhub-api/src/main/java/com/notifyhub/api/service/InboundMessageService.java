package com.notifyhub.api.service;

import com.notifyhub.api.dto.InboundMessageIngestRequest;
import com.notifyhub.api.dto.InboundMessageResponse;
import com.notifyhub.api.inbound.db.InboundMessageEntity;
import com.notifyhub.api.inbound.db.InboundMessageRepository;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class InboundMessageService {

    private final InboundMessageRepository repository;

    public InboundMessageService(InboundMessageRepository repository) {
        this.repository = repository;
    }

    public InboundMessageResponse ingest(InboundMessageIngestRequest req){
        OffsetDateTime receivedAt = (req.receivedAt() != null) ? req.receivedAt() : OffsetDateTime.now();

        InboundMessageEntity entity = InboundMessageEntity.builder()
                .channel(req.channel())
                .phoneNumber(req.phoneNumber())
                .body(req.body())
                .status(InboundMessageStatus.RECEIVED)
                .receivedAt(receivedAt)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        repository.save(entity);

        return new InboundMessageResponse(entity.getId(), entity.getStatus(), entity.getReceivedAt());

    }
}
