package com.mryqr.common.event.consume;

import org.springframework.transaction.annotation.Transactional;

// Make the event processing and event recording in the same transaction for situations where these two should be atomic
// Usually used when there is DB changes during event processing
// Best practices is to stick to AbstractTransactionalDomainEventHandler and idempotent as mush as possible
public abstract class AbstractTransactionalDomainEventHandler<T> extends AbstractDomainEventHandler<T> {

    @Override
    @Transactional
    public void handle(ConsumingDomainEvent<T> consumingDomainEvent) {
        super.handle(consumingDomainEvent);
    }
}
