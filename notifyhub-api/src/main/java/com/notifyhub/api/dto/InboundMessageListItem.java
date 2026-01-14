package com.notifyhub.api.dto;

import com.notifyhub.api.inbound.domain.InboundMessageStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InboundMessageListItem(
        UUID id,
        String channel,
        String phoneNumber,
        String body,
        InboundMessageStatus status,
        OffsetDateTime receivedAt
) { }
