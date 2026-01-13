package com.notifyhub.api.inbound.db;

import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(
        name = "inbound_message",
        indexes = {
                @Index(name = "idx_inbound_message_status", columnList = "status"),
                @Index(name = "idx_inbound_message_channel", columnList = "channel"),
                @Index(name = "idx_inbound_message_received_at", columnList = "received_at"),
                @Index(name = "idx_inbound_message_phone_number", columnList = "phone_number")
        }
)
public class InboundMessageEntity {

    @Id
    @UuidGenerator
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "channel", nullable = false, length = 20)
    private String channel;

    @Column(name = "phone_number", nullable = false, length = 32)
    private String phoneNumber;

    @Column(name = "body", nullable = false, columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InboundMessageStatus status;

    @Column(name = "received_at", nullable = false)
    private OffsetDateTime receivedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
