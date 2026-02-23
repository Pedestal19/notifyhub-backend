package com.notifyhub.api.service.admin;

import com.notifyhub.api.dto.admin.AdminInboundMessageDetails;
import com.notifyhub.api.dto.admin.AdminInboundMessageListItem;
import com.notifyhub.api.inbound.db.InboundMessageEntity;
import com.notifyhub.api.inbound.db.InboundMessageRepository;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AdminInboundMessageService {

    private final InboundMessageRepository inboundMessageRepository;

    public AdminInboundMessageService(InboundMessageRepository inboundMessageRepository) {
        this.inboundMessageRepository = inboundMessageRepository;
    }

    public Page<AdminInboundMessageListItem> list(
            InboundMessageStatus status,
            String phoneNumber,
            Pageable pageable
    ) {
        boolean hasPhone = phoneNumber != null && !phoneNumber.isBlank();
        Page<InboundMessageEntity> page;
        pageable = clamp(pageable);

        if (status != null && hasPhone) {
            page = inboundMessageRepository.findByStatusAndPhoneNumberOrderByReceivedAtDesc(status, phoneNumber, pageable);
        } else if (hasPhone) {
            page = inboundMessageRepository.findByPhoneNumberOrderByReceivedAtDesc(phoneNumber, pageable);
        } else if (status != null) {
            page = inboundMessageRepository.findByStatusOrderByReceivedAtDesc(status, pageable);
        } else {
            page = inboundMessageRepository.findAllByOrderByReceivedAtDesc(pageable);
        }

        return page.map(this::toListItem);
    }

    public AdminInboundMessageDetails get(UUID id) {
        InboundMessageEntity e = inboundMessageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Inbound message not found"
                ));
        return toDetails(e);
    }

    private AdminInboundMessageListItem toListItem(InboundMessageEntity e) {
        return new AdminInboundMessageListItem(
                e.getId(),
                e.getChannel(),
                e.getPhoneNumber(),
                e.getStatus(),
                e.getReceivedAt(),
                e.getUpdatedAt()
        );
    }

    private AdminInboundMessageDetails toDetails(InboundMessageEntity e) {
        return new AdminInboundMessageDetails(
                e.getId(),
                e.getChannel(),
                e.getPhoneNumber(),
                e.getBody(),
                e.getStatus(),
                e.getReceivedAt(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private Pageable clamp(Pageable pageable) {
        int size = Math.min(Math.max(pageable.getPageSize(), 1), 100);
        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}
