package com.notifyhub.worker.scheduler;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import java.time.Instant;

public class WorkerMetrics implements MeterBinder {

    private final InboundWorkerStats stats;

    public WorkerMetrics(InboundWorkerStats stats) {
        this.stats = stats;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("notifyhub.worker.last.claimed", stats, s -> s.lastClaimed()).register(registry);
        Gauge.builder("notifyhub.worker.last.processed", stats, s -> s.lastProcessed()).register(registry);
        Gauge.builder("notifyhub.worker.last.failed", stats, s -> s.lastFailed()).register(registry);
        Gauge.builder("notifyhub.worker.last.duration.ms", stats, s -> s.lastDurationMs()).register(registry);

        Gauge.builder("notifyhub.worker.last.success.epoch", stats, s -> {
            Instant t = s.lastSuccessAt().orElse(null);
            return t == null ? 0 : t.getEpochSecond();
        }).register(registry);
    }
}
