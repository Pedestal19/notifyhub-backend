package com.notifyhub.api.inbound.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InboundMessageRepository extends JpaRepository<InboundMessageEntity, UUID> {
}
