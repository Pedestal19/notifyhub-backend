package com.notifyhub.api.controller.admin;

import com.notifyhub.api.dto.admin.AdminInboundMessageDetails;
import com.notifyhub.api.dto.admin.AdminInboundMessageListItem;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import com.notifyhub.api.service.admin.AdminInboundMessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/inbound-messages")
public class AdminInboundMessageController {

    private final AdminInboundMessageService service;

    public AdminInboundMessageController(AdminInboundMessageService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AdminInboundMessageListItem> list(
            @RequestParam(required = false) InboundMessageStatus status,
            @RequestParam(required = false) String phoneNumber,
            Pageable pageable
    ) {
        return service.list(status, phoneNumber, pageable);
    }

    @GetMapping("/{id}")
    public AdminInboundMessageDetails get(@PathVariable UUID id) {
        return service.get(id);
    }
}