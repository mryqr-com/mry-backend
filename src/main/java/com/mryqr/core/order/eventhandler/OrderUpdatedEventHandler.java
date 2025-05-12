package com.mryqr.core.order.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.order.domain.event.OrderUpdatedEvent;
import com.mryqr.core.order.domain.task.SyncOrderToManagedQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderUpdatedEventHandler implements DomainEventHandler {
    private final SyncOrderToManagedQrTask syncOrderToManagedQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent instanceof OrderUpdatedEvent;
    }

    @Override
    @Transactional
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        OrderUpdatedEvent theEvent = (OrderUpdatedEvent) domainEvent;
        taskRunner.run(() -> syncOrderToManagedQrTask.sync(theEvent.getOrderId()));
    }
}
