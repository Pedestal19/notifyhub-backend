package com.notifyhub.worker.service;

import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class InboundMessageProcessor {

    private final InboundMessageRepository inboundMessageRepository;

    public InboundMessageProcessor(InboundMessageRepository inboundMessageRepository) {
        this.inboundMessageRepository = inboundMessageRepository;
    }

    @Transactional
    public int processBatch(int limit) {
        var page = inboundMessageRepository.findByStatusOrderByReceivedAtAsc(
                InboundMessageStatus.RECEIVED,
                PageRequest.of(0, Math.min(limit, 100))
        );

        for (var msg : page) {
            msg.setStatus(InboundMessageStatus.PROCESSING);
            msg.setUpdatedAt(OffsetDateTime.now());
            inboundMessageRepository.save(msg);

            // v1 stub "work"
            msg.setStatus(InboundMessageStatus.PROCESSED);
            msg.setUpdatedAt(OffsetDateTime.now());
            inboundMessageRepository.save(msg);
        }

        return page.getNumberOfElements();
    }
}
