package com.notifyhub.worker.scheduler;

import com.notifyhub.worker.service.InboundMessageProcessor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class InboundMessageJob {

    private final InboundMessageProcessor inboundMessageProcessor;

    public InboundMessageJob(InboundMessageProcessor inboundMessageProcessor) {
        this.inboundMessageProcessor = inboundMessageProcessor;
    }

    @Scheduled(fixedDelayString = "${notifyhub.worker.poll-delay-ms:5000}")
    public void run() {
        inboundMessageProcessor.processBatch(50);
    }
}
