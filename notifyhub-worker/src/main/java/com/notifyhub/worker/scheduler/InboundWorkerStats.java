package com.notifyhub.worker.scheduler;

import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Getter
public class InboundWorkerStats {

    private final AtomicReference<OffsetDateTime> lastRunAt = new AtomicReference<>();
    private final AtomicLong lastDurationMs = new AtomicLong(0);
    private final AtomicLong lastClaimed = new AtomicLong(0);
    private final AtomicLong lastFailed = new AtomicLong(0);

    private final AtomicLong totalClaimed = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);

    private final AtomicReference<Throwable> lastError = new AtomicReference<>();

    public void recordSuccess(long claimed, long failed, long durationMs) {
        lastRunAt.set(OffsetDateTime.now());
        lastDurationMs.set(durationMs);

        lastClaimed.set(claimed);
        lastFailed.set(failed);

        totalClaimed.addAndGet(claimed);
        totalFailed.addAndGet(failed);

        lastError.set(null);
    }

    public void recordFailure(Throwable t, long durationMs) {
        lastRunAt.set(OffsetDateTime.now());
        lastDurationMs.set(durationMs);
        lastError.set(t);
    }
}
