package com.notifyhub.worker.scheduler;

import com.notifyhub.worker.service.BatchResult;
import lombok.Getter;

import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class InboundWorkerStats {

    private final Clock clock;

    private final AtomicReference<Instant> lastRunAt = new AtomicReference<>(null);
    private final AtomicReference<Instant> lastSuccessAt = new AtomicReference<>(null);
    private final AtomicReference<Instant> lastFailureAt = new AtomicReference<>(null);

    private final AtomicLong lastDurationMs = new AtomicLong(0);
    private final AtomicLong lastClaimed = new AtomicLong(0);
    private final AtomicLong lastProcessed = new AtomicLong(0);
    private final AtomicLong lastFailed = new AtomicLong(0);

    private final AtomicReference<Throwable> lastError = new AtomicReference<>();

    public InboundWorkerStats(Clock clock) {
        this.clock = clock;
    }

    public void recordSuccess(BatchResult result, long durationMs) {
        Instant now = clock.instant();
        lastRunAt.set(now);
        lastSuccessAt.set(now);

        lastDurationMs.set(durationMs);
        lastClaimed.set(result.claimed());
        lastProcessed.set(result.processed());
        lastFailed.set(result.failed());
    }

    public void recordFailure(long durationMs) {
        Instant now = clock.instant();
        lastRunAt.set(now);
        lastFailureAt.set(now);
        lastDurationMs.set(durationMs);
    }
}
