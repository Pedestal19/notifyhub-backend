package com.notifyhub.api.service;

import com.notifyhub.api.dto.InboundMessageIngestRequest;
import com.notifyhub.api.dto.InboundMessageResponse;
import com.notifyhub.api.inbound.db.InboundMessageEntity;
import com.notifyhub.api.inbound.db.InboundMessageRepository;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

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

    public List<InboundMessageResponse> list(InboundMessageStatus status, int limit) {
        var pageable = PageRequest.of(0, Math.min(limit, 100));

        var page = (status == null)
                ? repository.findAllByOrderByReceivedAtDesc(pageable)
                : repository.findByStatusOrderByReceivedAtDesc(status, pageable);

        return page.stream()
                .map(e -> new InboundMessageResponse(e.getId(), e.getStatus(), e.getReceivedAt()))
                .toList();
    }
}
