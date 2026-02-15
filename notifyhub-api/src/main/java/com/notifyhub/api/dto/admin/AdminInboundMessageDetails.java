package com.notifyhub.api.dto.admin;

import com.notifyhub.api.inbound.domain.InboundMessageStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminInboundMessageDetails(
        UUID id,
        String channel,
        String phoneNumber,
        String body,
        InboundMessageStatus status,
        OffsetDateTime receivedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
        ) {}
