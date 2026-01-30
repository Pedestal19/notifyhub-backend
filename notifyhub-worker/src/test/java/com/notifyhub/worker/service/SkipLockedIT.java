package com.notifyhub.worker.service;

import com.notifyhub.worker.inbound.db.InboundMessageEntity;
import com.notifyhub.worker.inbound.db.InboundMessageRepository;
import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class SkipLockedIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired InboundMessageRepository repo;
    @Autowired InboundMessageClaimer claimer;

    @BeforeEach
    void clean() {
        repo.deleteAll();
    }

    @Test
    void twoConcurrentClaims_doNotOverlap() throws Exception {
        for (int i = 0; i < 10; i++) {
            repo.save(InboundMessageEntity.builder()
                    .status(InboundMessageStatus.RECEIVED)
                    .body("m" + i)
                    .receivedAt(OffsetDateTime.now().minusMinutes(10).plusSeconds(i))
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .channel("SMS").phoneNumber("+1")
                    .build());
        }

        ExecutorService pool = Executors.newFixedThreadPool(2);

        Future<List<InboundMessageEntity>> f1 = pool.submit(() -> claimer.claimReceived(6));
        Future<List<InboundMessageEntity>> f2 = pool.submit(() -> claimer.claimReceived(6));

        var a = f1.get(5, TimeUnit.SECONDS);
        var b = f2.get(5, TimeUnit.SECONDS);

        Set<?> idsA = a.stream().map(InboundMessageEntity::getId).collect(Collectors.toSet());
        Set<?> idsB = b.stream().map(InboundMessageEntity::getId).collect(Collectors.toSet());

        idsA.retainAll(idsB);
        assertThat(idsA).isEmpty();

        pool.shutdownNow();
    }
}
