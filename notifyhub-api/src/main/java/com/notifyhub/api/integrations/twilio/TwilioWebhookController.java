package com.notifyhub.api.integrations.twilio;

import com.notifyhub.api.dto.InboundMessageResponse;
import com.notifyhub.api.integrations.twilio.dto.TwilioInboundForm;
import com.notifyhub.api.service.InboundMessageService;
import org.springframework.http.HttpStatus;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/webhooks/twilio")
public class TwilioWebhookController {

    private final InboundMessageService inboundMessageService;
    private final TwilioInboundMapper twilioInboundMapper;

    public TwilioWebhookController(InboundMessageService inboundMessageService, TwilioInboundMapper twilioInboundMapper) {
        this.inboundMessageService = inboundMessageService;
        this.twilioInboundMapper = twilioInboundMapper;
    }

    @PostMapping(consumes = "application/x-www-form-urlencoded")
    @ResponseStatus(HttpStatus.CREATED)
    public InboundMessageResponse inbound(@RequestParam MultiValueMap<String, String> form) {
        String from = form.getFirst("From");
        String body = form.getFirst("Body");
        String to = form.getFirst("To");
        String messageSid = form.getFirst("MessageSid");


        if (from == null || from.isBlank() || body == null || body.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing From or Body");
        }

        TwilioInboundForm twilio = new TwilioInboundForm(from, to, body, messageSid);

        return inboundMessageService.ingest(twilioInboundMapper.toCanonical(twilio));
    }
}
