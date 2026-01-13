package com.notifyhub.api.inbound.db;

import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface InboundMessageRepository extends JpaRepository<InboundMessageEntity, UUID> {

    Page<InboundMessageEntity> findByStatusOrderByReceivedAtDesc(InboundMessageStatus status, Pageable pageable);

    Page<InboundMessageEntity> findByPhoneNumberOrderByReceivedAtDesc(String phoneNumber, Pageable pageable);

    Page<InboundMessageEntity> findAllByOrderByReceivedAtDesc(Pageable pageable);

    long countByStatus(InboundMessageStatus status);

    long countByReceivedAtAfter(OffsetDateTime after);
}
