package com.notifyhub.api.dto.admin;

import com.notifyhub.api.inbound.domain.InboundMessageStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminInboundMessageListItem(
        UUID id,
        String channel,
        String phoneNumber,
        InboundMessageStatus status,
        OffsetDateTime receivedAt,
        OffsetDateTime updatedAt
        ) {}
