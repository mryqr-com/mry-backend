package com.mryqr.core.order.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.order.domain.event.OrderCreatedEvent;
import com.mryqr.core.order.domain.task.SyncOrderToManagedQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventHandler extends AbstractDomainEventHandler<OrderCreatedEvent> {
    private final SyncOrderToManagedQrTask syncOrderToManagedQrTask;

    @Override
    public void handle(OrderCreatedEvent event) {
        MryTaskRunner.run(() -> syncOrderToManagedQrTask.sync(event.getOrderId()));

    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
