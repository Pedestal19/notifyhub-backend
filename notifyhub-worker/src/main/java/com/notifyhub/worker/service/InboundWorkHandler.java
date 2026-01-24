package com.notifyhub.worker.service;

import com.notifyhub.worker.inbound.db.InboundMessageEntity;

@FunctionalInterface
public interface InboundWorkHandler {
    void handle(InboundMessageEntity msg);
}
