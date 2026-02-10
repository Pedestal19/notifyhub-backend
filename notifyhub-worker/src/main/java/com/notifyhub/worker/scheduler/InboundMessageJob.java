package com.notifyhub.worker.scheduler;

import com.notifyhub.worker.configuration.WorkerProperties;
import com.notifyhub.worker.service.BatchResult;
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
    private final InboundWorkerStats stats;


    public InboundMessageJob(InboundMessageProcessor inboundMessageProcessor, WorkerProperties props, InboundWorkerStats stats) {
        this.inboundMessageProcessor = inboundMessageProcessor;
        this.props = props;
        this.stats = stats;
    }

    @Scheduled(fixedDelayString = "${notifyhub.worker.poll-delay-ms:5000}")
    public void run() {
        if (!running.compareAndSet(false, true)) {
            log.debug("Skipping run: previous run still in progress");
            return;
        }

        long start = System.nanoTime();
        try {
            BatchResult result = inboundMessageProcessor.processBatch(props.batchSize());
            long ms = (System.nanoTime() - start) / 1_000_000;
            stats.recordSuccess(result, ms);

            log.info("Worker tick done: claimed={} processed={} failed={} durationMs={}",
                    result.claimed(), result.processed(), result.failed(), ms);
        } catch (Exception ex) {
            long ms = (System.nanoTime() - start) / 1_000_000;
            stats.recordFailure(ms, ex);
            log.error("Worker tick failed (durationMs={})", ms, ex);
        } finally {
            running.set(false);
        }
    }
}
