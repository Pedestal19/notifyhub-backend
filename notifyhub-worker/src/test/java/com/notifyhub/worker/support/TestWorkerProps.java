package com.notifyhub.worker.support;

import org.springframework.test.context.DynamicPropertyRegistry;

public class TestWorkerProps {
    private TestWorkerProps() {}

    public static void addWorkerProps(DynamicPropertyRegistry r) {
        r.add("notifyhub.worker.max-page-size", () -> "100");
        r.add("notifyhub.worker.retry-after", () -> "PT2M");
        r.add("notifyhub.worker.poll-delay-ms", () -> "500");
        r.add("notifyhub.worker.batch-size", () -> "50");
        r.add("notifyhub.worker.health-stale-after", () -> "PT60S");
    }
}
