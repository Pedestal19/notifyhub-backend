package com.notifyhub.worker.service;

import com.notifyhub.worker.configuration.WorkerProperties;
import com.notifyhub.worker.inbound.db.InboundMessageEntity;
import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class InboundMessageClaimer {

    private static final Logger log = LoggerFactory.getLogger(InboundMessageClaimer.class);

    private final InboundMessageRepository repo;
    private final WorkerProperties props;

    public InboundMessageClaimer(InboundMessageRepository repo, WorkerProperties props) {
        this.repo = repo;
        this.props = props;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<InboundMessageEntity> claimReceived(int limit) {
        List<InboundMessageEntity> msgs =
                repo.claimByStatusSkipLocked(InboundMessageStatus.RECEIVED.name(), limit);

        if (msgs.isEmpty()) return List.of();

        OffsetDateTime now = OffsetDateTime.now();
        msgs.forEach(m -> {
            m.setStatus(InboundMessageStatus.PROCESSING);
            m.setUpdatedAt(now);
        });
        repo.saveAll(msgs);

        log.info("Claimed {} RECEIVED -> PROCESSING", msgs.size());
        return msgs;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<InboundMessageEntity> claimStuckProcessing(int limit) {
        OffsetDateTime cutoff = OffsetDateTime.now().minus(props.retryAfter());

        List<InboundMessageEntity> msgs =
                repo.claimStuckProcessingSkipLocked(InboundMessageStatus.PROCESSING.name(), cutoff, limit);

        if (msgs.isEmpty()) return List.of();

        OffsetDateTime now = OffsetDateTime.now();
        msgs.forEach(m -> m.setUpdatedAt(now));
        repo.saveAll(msgs);

        log.warn("Claimed {} stuck PROCESSING messages (cutoff={})", msgs.size(), cutoff);
        return msgs;
    }
}
