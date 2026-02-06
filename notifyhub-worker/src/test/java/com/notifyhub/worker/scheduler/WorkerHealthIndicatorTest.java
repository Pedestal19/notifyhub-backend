package com.notifyhub.worker.scheduler;

import com.notifyhub.worker.configuration.WorkerProperties;
import com.notifyhub.worker.service.BatchResult;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

public class WorkerHealthIndicatorTest {

    static class MutableClock extends Clock {
        private final AtomicReference<Instant> now;
        MutableClock(Instant start) {
            this.now = new AtomicReference<>(start);
        }
        void set(Instant t) {
            now.set(t);
        }
        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }
        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }
        @Override
        public Instant instant() {
            return now.get();
        }
    }

    @Test
    void health_is_down_when_stale() {
        MutableClock clock = new MutableClock(Instant.parse("2026-02-05T00:00:00Z"));

        WorkerProperties props = new WorkerProperties(
                100,
                Duration.ofMinutes(2),
                5000L,
                50,
                Duration.ofSeconds(60)
        );

        InboundWorkerStats stats = new InboundWorkerStats(clock);
        WorkerHealthIndicator health = new WorkerHealthIndicator(stats, props, clock);

        stats.recordSuccess(new BatchResult(1, 1, 0), 12);

        clock.set(Instant.parse("2026-02-05T00:02:00Z"));

        assertThat(health.health().getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.health().getDetails().get("reason")).isEqualTo("stale");
    }
}
