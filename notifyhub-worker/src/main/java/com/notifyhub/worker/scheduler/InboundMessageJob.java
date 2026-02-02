package com.notifyhub.worker.scheduler;

import com.notifyhub.worker.configuration.WorkerProperties;
import com.notifyhub.worker.service.InboundMessageProcessor;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class InboundMessageJob {

    private static final Logger log = LoggerFactory.getLogger(InboundMessageJob.class);

    private final InboundMessageProcessor inboundMessageProcessor;
    private final WorkerProperties props;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public InboundMessageJob(InboundMessageProcessor inboundMessageProcessor, WorkerProperties props) {
        this.inboundMessageProcessor = inboundMessageProcessor;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${notifyhub.worker.poll-delay-ms:5000}")
    public void run() {
        if (!running.compareAndSet(false, true)) {
            log.debug("Skipping run: previous run still in progress");
            return;
        }

        long start = System.nanoTime();
        try {
            int claimed = inboundMessageProcessor.processBatch(props.batchSize());
            long ms = (System.nanoTime() - start) / 1_000_000;
            log.info("Worker tick done: claimed={} durationMs={}", claimed, ms);
        } catch (Exception ex) {
            log.error("Worker tick failed", ex);
        } finally {
            running.set(false);
        }
    }
}
