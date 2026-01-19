package com.notifyhub.api.integrations.twilio;

import com.notifyhub.api.dto.InboundMessageIngestRequest;
import com.notifyhub.api.integrations.twilio.dto.TwilioInboundForm;
import org.springframework.stereotype.Component;

@Component
public class TwilioInboundMapper {
    public InboundMessageIngestRequest toCanonical(TwilioInboundForm form) {
        return new InboundMessageIngestRequest(
                "SMS",
                form.From(),
                form.Body(),
                null
        );
    }
}
