package com.notifyhub.worker.scheduler;

import com.notifyhub.worker.service.BatchResult;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class InboundWorkerStats {

    private final Clock clock;

    private final AtomicReference<Instant> lastRunAt = new AtomicReference<>(null);
    private final AtomicReference<Instant> lastSuccessAt = new AtomicReference<>(null);
    private final AtomicReference<Instant> lastFailureAt = new AtomicReference<>(null);

    private final AtomicLong lastDurationMs = new AtomicLong(0);
    private final AtomicLong lastClaimed = new AtomicLong(0);
    private final AtomicLong lastProcessed = new AtomicLong(0);
    private final AtomicLong lastFailed = new AtomicLong(0);

    private final AtomicReference<String> lastError = new AtomicReference<>();

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

    public void recordFailure(long durationMs, Throwable ex) {
        Instant now = clock.instant();
        lastRunAt.set(now);
        lastFailureAt.set(now);
        lastDurationMs.set(durationMs);
        lastError.set(ex == null ? "unknown" : ex.getClass().getSimpleName() + ": " + ex.getMessage());
    }

    public Optional<Instant> lastRunAt() { return Optional.ofNullable(lastRunAt.get()); }
    public Optional<Instant> lastSuccessAt() { return Optional.ofNullable(lastSuccessAt.get()); }
    public Optional<Instant> lastFailureAt() { return Optional.ofNullable(lastFailureAt.get()); }
    public Optional<String> lastError() { return Optional.ofNullable(lastError.get()); }

    public long lastClaimed() { return lastClaimed.get(); }
    public long lastProcessed() { return lastProcessed.get(); }
    public long lastFailed() { return lastFailed.get(); }
    public long lastDurationMs() { return lastDurationMs.get(); }
}
