package com.mryqr.common.event.consume;

public interface ConsumingDomainEventDao<T> {
    // return true means this event has never been consumed before
    boolean recordAsConsumed(ConsumingDomainEvent<T> consumingDomainEvent, String handlerName);
}
