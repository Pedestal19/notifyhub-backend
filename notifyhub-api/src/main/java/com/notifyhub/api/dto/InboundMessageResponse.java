package com.notifyhub.api.dto;

import com.notifyhub.api.inbound.domain.InboundMessageStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InboundMessageResponse(
        UUID id,
        InboundMessageStatus status,
        OffsetDateTime receivedAt
) {
}
