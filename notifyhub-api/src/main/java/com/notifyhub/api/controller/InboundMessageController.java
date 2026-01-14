package com.notifyhub.api.controller;

import com.notifyhub.api.dto.InboundMessageIngestRequest;
import com.notifyhub.api.dto.InboundMessageListItem;
import com.notifyhub.api.dto.InboundMessageResponse;
import com.notifyhub.api.inbound.db.InboundMessageRepository;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import com.notifyhub.api.service.InboundMessageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class InboundMessageController {

    private final InboundMessageService inboundMessageService;
    private final InboundMessageRepository inboundMessageRepository;

    public InboundMessageController(InboundMessageService inboundMessageService, InboundMessageRepository inboundMessageRepository) {
        this.inboundMessageService = inboundMessageService;
        this.inboundMessageRepository = inboundMessageRepository;
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

    @GetMapping("/v1/inbound-messages")
    public List<InboundMessageListItem> list(
            @RequestParam(required = false) InboundMessageStatus status,
            @RequestParam(defaultValue = "20") int limit
    ){
        return inboundMessageService.list(status, limit);
    }
}
