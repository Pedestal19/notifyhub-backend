package com.notifyhub.worker.scheduler;

import com.notifyhub.worker.configuration.WorkerProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class WorkerHealthIndicator implements HealthIndicator {

    private final InboundWorkerStats stats;
    private final WorkerProperties props;
    private final Clock clock;

    public WorkerHealthIndicator(InboundWorkerStats stats, WorkerProperties props, Clock clock) {
        this.stats = stats;
        this.props = props;
        this.clock = clock;
    }

    @Override
    public Health health() {
        Duration staleAfter = props.healthStaleAfter();
        Instant lastSuccess = stats.lastSuccessAt();
        if (lastSuccess == null) {
            return Health.down()
                    .withDetail("reason", "no successful run yet")
                    .build();
        }

        Duration age = Duration.between(lastSuccess, clock.instant());
        if (age.compareTo(staleAfter) > 0) {
            return Health.down()
                    .withDetail("reason", "stale")
                    .withDetail("ageSeconds", age.toSeconds())
                    .withDetail("staleAfterSeconds", staleAfter.toSeconds())
                    .build();
        }

        return Health.up()
                .withDetail("lastSuccessAt", lastSuccess.toString())
                .withDetail("lastClaimed", stats.lastClaimed())
                .withDetail("lastProcessed", stats.lastProcessed())
                .withDetail("lastFailed", stats.lastFailed())
                .build();
    }
}
