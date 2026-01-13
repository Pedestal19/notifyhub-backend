package com.notifyhub.api.controller;

import com.notifyhub.api.dto.InboundMessageIngestRequest;
import com.notifyhub.api.dto.InboundMessageResponse;
import com.notifyhub.api.inbound.db.InboundMessageRepository;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import com.notifyhub.api.service.InboundMessageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public List<InboundMessageResponse> list(
            @RequestParam(required = false) InboundMessageStatus status,
            @RequestParam(defaultValue = "20") int limit
    ){
        var pageable = PageRequest.of(0, Math.min(limit, 100));

        var page = (status == null)
                ? inboundMessageRepository.findAll(pageable)
                : inboundMessageRepository.findByStatusOrderByReceivedAtDesc(status, pageable);

        return page.stream()
                .map(e -> new InboundMessageResponse(e.getId(), e.getStatus(), e.getReceivedAt()))
                .toList();
    }
}
