package com.notifyhub.api.integrations.twilio.dto;

public record TwilioInboundForm(
        String From,
        String To,
        String Body,
        String MessageSid
) {
}
