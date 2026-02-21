package com.notifyhub.api.service.admin;

import com.notifyhub.api.dto.admin.AdminInboundMessageDetails;
import com.notifyhub.api.dto.admin.AdminInboundMessageListItem;
import com.notifyhub.api.inbound.db.InboundMessageEntity;
import com.notifyhub.api.inbound.db.InboundMessageRepository;
import com.notifyhub.api.inbound.domain.InboundMessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminInboundMessageService {

    private final InboundMessageRepository repo;

    public AdminInboundMessageService(InboundMessageRepository repo) {
        this.repo = repo;
    }

    public Page<AdminInboundMessageListItem> list(
            InboundMessageStatus status,
            String phoneNumber,
            Pageable pageable
    ) {
        boolean hasPhone = phoneNumber != null && !phoneNumber.isBlank();
        Page<InboundMessageEntity> page;

        if (status != null && hasPhone) {
            page = repo.findByStatusAndPhoneNumberOrderByReceivedAtDesc(status, phoneNumber, pageable);
        } else if (hasPhone) {
            page = repo.findByPhoneNumberOrderByReceivedAtDesc(phoneNumber, pageable);
        } else if (status != null) {
            page = repo.findByStatusOrderByReceivedAtDesc(status, pageable);
        } else {
            page = repo.findAllByOrderByReceivedAtDesc(pageable);
        }

        return page.map(this::toListItem);
    }

    public AdminInboundMessageDetails get(UUID id) {
        InboundMessageEntity e = repo.findById(id).orElseThrow();
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
}
