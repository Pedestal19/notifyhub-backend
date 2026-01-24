package com.notifyhub.worker.service;

import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

@Service
public class InboundMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(InboundMessageProcessor.class);

    private final InboundMessageRepository inboundMessageRepository;
    private final InboundWorkHandler inboundWorkHandler;

    public InboundMessageProcessor(InboundMessageRepository inboundMessageRepository, InboundWorkHandler inboundWorkHandler) {
        this.inboundMessageRepository = inboundMessageRepository;
        this.inboundWorkHandler = inboundWorkHandler;
    }

    @Transactional
    public int processBatch(int limit) {
        var page = inboundMessageRepository.findByStatusOrderByReceivedAtAsc(
                InboundMessageStatus.RECEIVED,
                PageRequest.of(0, Math.min(limit, 100))
        );

        var msgs = page.getContent();
        if (msgs.isEmpty()) {
            log.debug("No RECEIVED messages found");
            return 0;
        }

        log.info("Found {} RECEIVED messages (limit={})", msgs.size(), limit);

        var now = OffsetDateTime.now();
        msgs.forEach(m -> {
            m.setStatus(InboundMessageStatus.PROCESSING);
            m.setUpdatedAt(now);
        });
        inboundMessageRepository.saveAll(msgs);

        // v1 stub work (later: call processor/handler)
        var doneAt = OffsetDateTime.now();
        msgs.forEach(m -> {
            try {
                inboundWorkHandler.handle(m);
                m.setStatus(InboundMessageStatus.PROCESSED);
            } catch (Exception ex) {
                log.warn("Processing failed for message id={}", m.getId(), ex);
                m.setStatus(InboundMessageStatus.FAILED);
            }
            m.setUpdatedAt(doneAt);
        });
        inboundMessageRepository.saveAll(msgs);

        log.info("Finished batch: processed={} failed={}",
                msgs.stream().filter(x -> x.getStatus() == InboundMessageStatus.PROCESSED).count(),
                msgs.stream().filter(x -> x.getStatus() == InboundMessageStatus.FAILED).count()
        );
        return msgs.size();
    }
}
