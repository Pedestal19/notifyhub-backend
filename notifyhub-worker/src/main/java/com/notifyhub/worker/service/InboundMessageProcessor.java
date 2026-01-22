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

        var now = OffsetDateTime.now();

        var msgs = page.getContent();
        msgs.forEach(m -> {
            m.setStatus(InboundMessageStatus.PROCESSING);
            m.setUpdatedAt(now);
        });
        inboundMessageRepository.saveAll(msgs);

        // v1 stub work (later: call processor/handler)
        var doneAt = OffsetDateTime.now();
        msgs.forEach(m -> {
            m.setStatus(InboundMessageStatus.PROCESSED);
            m.setUpdatedAt(doneAt);
        });
        inboundMessageRepository.saveAll(msgs);

        return msgs.size();
    }
}
