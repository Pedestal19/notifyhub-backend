package com.notifyhub.api.integrations.twilio;

import com.notifyhub.api.dto.InboundMessageResponse;
import com.notifyhub.api.integrations.twilio.dto.TwilioInboundForm;
import com.notifyhub.api.service.InboundMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks/twilio")
public class TwilioWebhookController {

    private final InboundMessageService inboundMessageService;
    private final TwilioInboundMapper twilioInboundMapper = new TwilioInboundMapper();

    public TwilioWebhookController(InboundMessageService inboundMessageService) {
        this.inboundMessageService = inboundMessageService;
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    @ResponseStatus(HttpStatus.CREATED)
    public InboundMessageResponse inbound(@RequestParam MultiValueMap<String, String> form) {
        TwilioInboundForm twilio = new TwilioInboundForm(
                form.getFirst("From"),
                form.getFirst("To"),
                form.getFirst("Body"),
                form.getFirst("MessageSid")
        );

        return inboundMessageService.ingest(twilioInboundMapper.toCanonical(twilio));
    }
}
