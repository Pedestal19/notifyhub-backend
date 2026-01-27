package com.notifyhub.worker.service;

import com.notifyhub.worker.configuration.WorkerProperties;
import com.notifyhub.worker.inbound.db.InboundMessageEntity;
import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InboundMessageProcessor {

    private static final Logger log = LoggerFactory.getLogger(InboundMessageProcessor.class);

    private final InboundMessageRepository inboundMessageRepository;
    private final InboundWorkHandler inboundWorkHandler;
    private final WorkerProperties props;

    public InboundMessageProcessor(InboundMessageRepository inboundMessageRepository, InboundWorkHandler inboundWorkHandler, WorkerProperties props) {
        this.inboundMessageRepository = inboundMessageRepository;
        this.inboundWorkHandler = inboundWorkHandler;
        this.props = props;
    }

    @Transactional
    public int processBatch(int limit) {
        int effectiveLimit = Math.min(limit, props.maxPageSize());
        if (effectiveLimit <= 0) return 0;

        int processedTotal = 0;

        processedTotal += processReceived(effectiveLimit - processedTotal);

        if (processedTotal < effectiveLimit) {
            processedTotal += processStuckProcessing(effectiveLimit - processedTotal);
        }

        if (processedTotal == 0) {
            log.debug("No messages to process (limit={}, maxPageSize={}, retryAfter={})",
                    limit, props.maxPageSize(), props.retryAfter());
        } else {
            log.info("Batch done. totalProcessed={} (limit={}, maxPageSize={})",
                    processedTotal, limit, props.maxPageSize());
        }

        return processedTotal;
    }

    private int processReceived(int remaining) {
        if (remaining <= 0) return 0;

        var page = inboundMessageRepository.findByStatusOrderByReceivedAtAsc(
                InboundMessageStatus.RECEIVED,
                PageRequest.of(0, remaining)
        );

        var msgs = page.getContent();
        if (msgs.isEmpty()) return 0;

        log.info("Found {} RECEIVED messages (remaining={})", msgs.size(), remaining);
        processMessages(msgs);
        return msgs.size();
    }

    private int processStuckProcessing(int remaining) {
        if (remaining <= 0) return 0;

        var now = OffsetDateTime.now();
        var cutoff = now.minus(props.retryAfter());

        var page = inboundMessageRepository.findByStatusAndUpdatedAtBeforeOrderByReceivedAtAsc(
                InboundMessageStatus.PROCESSING,
                cutoff,
                PageRequest.of(0, remaining)
        );

        var msgs = page.getContent();
        if (msgs.isEmpty()) return 0;

        log.warn("Retrying {} stuck PROCESSING messages (cutoff={}, remaining={})",
                msgs.size(), cutoff, remaining);
        processMessages(msgs);
        return msgs.size();
    }

    private void processMessages(List<InboundMessageEntity> msgs) {
        var processingAt = OffsetDateTime.now();
        msgs.forEach(m -> {
            m.setStatus(InboundMessageStatus.PROCESSING);
            m.setUpdatedAt(processingAt);
        });
        inboundMessageRepository.saveAll(msgs);

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
    }
}
