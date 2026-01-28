package com.notifyhub.worker.inbound.db;

import com.notifyhub.worker.inbound.domain.InboundMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface InboundMessageRepository extends JpaRepository<InboundMessageEntity, UUID> {

    Page<InboundMessageEntity> findByStatusOrderByReceivedAtDesc(InboundMessageStatus status, Pageable pageable);

    Page<InboundMessageEntity> findByPhoneNumberOrderByReceivedAtDesc(String phoneNumber, Pageable pageable);

    Page<InboundMessageEntity> findAllByOrderByReceivedAtDesc(Pageable pageable);

    long countByStatus(InboundMessageStatus status);

    long countByReceivedAtAfter(OffsetDateTime after);

    Page<InboundMessageEntity> findByStatusOrderByReceivedAtAsc(InboundMessageStatus status, Pageable pageable);

    Page<InboundMessageEntity> findByStatusAndUpdatedAtBeforeOrderByReceivedAtAsc(
            InboundMessageStatus status,
            OffsetDateTime cutoff,
            Pageable pageable
    );

    @Query(value = """
        select *
        from inbound_message
        where status = :status
        order by received_at asc
        for update skip locked
        limit :limit
        """, nativeQuery = true)
    List<InboundMessageEntity> claimByStatusSkipLocked(
            @Param("status") String status,
            @Param("limit") int limit
    );

    @Query(value = """
        select *
        from inbound_message
        where status = :status
          and updated_at < :cutoff
        order by received_at asc
        for update skip locked
        limit :limit
        """, nativeQuery = true)
    List<InboundMessageEntity> claimStuckProcessingSkipLocked(
            @Param("status") String status,
            @Param("cutoff") OffsetDateTime cutoff,
            @Param("limit") int limit
    );
}
