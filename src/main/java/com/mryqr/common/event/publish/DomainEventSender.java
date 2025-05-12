package com.mryqr.common.event.publish;

import com.mryqr.common.event.DomainEvent;

import java.util.concurrent.CompletableFuture;

public interface DomainEventSender {
    CompletableFuture<String> send(DomainEvent domainEvent);
}
