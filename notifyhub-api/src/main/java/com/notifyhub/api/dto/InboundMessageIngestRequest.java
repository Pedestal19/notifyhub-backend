package com.notifyhub.api.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

public record InboundMessageIngestRequest(
        @NotBlank String channel,         //Keep for v1; move later to an enum
        @NotBlank String phoneNumber,
        @NotBlank String body,
        OffsetDateTime receivedAt   // Optional: if provider sends timestamp; else we set server time
        ) {
}
