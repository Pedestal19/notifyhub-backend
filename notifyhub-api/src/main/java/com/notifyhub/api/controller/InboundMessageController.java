package com.notifyhub.api.controller;

import com.notifyhub.api.dto.InboundMessageIngestRequest;
import com.notifyhub.api.dto.InboundMessageResponse;
import com.notifyhub.api.service.InboundMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InboundMessageController {

    private final InboundMessageService inboundMessageService;

    public InboundMessageController(InboundMessageService inboundMessageService) {
        this.inboundMessageService = inboundMessageService;
    }

    @PostMapping("/v1/inbound-messages")
    @ResponseStatus(HttpStatus.CREATED)
    public InboundMessageResponse ingestV1(@Valid @RequestBody InboundMessageIngestRequest request) {
        return inboundMessageService.ingest(request);
    }

    @PostMapping("/webhooks/inbound")
    @ResponseStatus(HttpStatus.CREATED)
    public InboundMessageResponse ingestWebhook(@Valid @RequestBody InboundMessageIngestRequest request) {
        return inboundMessageService.ingest(request);
    }
}
