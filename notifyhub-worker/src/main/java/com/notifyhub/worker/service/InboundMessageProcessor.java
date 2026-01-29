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
    private final InboundMessageClaimer claimer;


    public InboundMessageProcessor(InboundMessageRepository inboundMessageRepository, InboundWorkHandler inboundWorkHandler, WorkerProperties props, InboundMessageClaimer claimer) {
        this.inboundMessageRepository = inboundMessageRepository;
        this.inboundWorkHandler = inboundWorkHandler;
        this.props = props;
        this.claimer = claimer;
    }

    @Transactional
    public int processBatch(int limit) {
        int effectiveLimit = Math.min(limit, props.maxPageSize());
        if (effectiveLimit <= 0) return 0;

        List<InboundMessageEntity> claimed = claimer.claimReceived(effectiveLimit);

        if (claimed.isEmpty()) {
            claimed = claimer.claimStuckProcessing(effectiveLimit);
        }

        if (claimed.isEmpty()) return 0;

        finalizeBatch(claimed);

        return claimed.size();
    }

    @Transactional
    public void finalizeBatch(List<InboundMessageEntity> msgs) {
        var doneAt = OffsetDateTime.now();

        for (var m : msgs) {
            try {
                inboundWorkHandler.handle(m);
                m.setStatus(InboundMessageStatus.PROCESSED);
            } catch (Exception ex) {
                m.setStatus(InboundMessageStatus.FAILED);
            }
            m.setUpdatedAt(doneAt);
        }

        inboundMessageRepository.saveAll(msgs);
    }}
