package com.notifyhub.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InboundMessageResponse(
        UUID id,
        String status,
        OffsetDateTime receivedAt
) {
}
