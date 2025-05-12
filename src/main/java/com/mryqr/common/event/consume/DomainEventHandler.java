package com.mryqr.common.event.consume;

public interface DomainEventHandler<T> {

    default boolean isIdempotent() {
        return false; // By default, all handlers are assumed to be not idempotent
    }

    default int priority() {
        return 0; // Smaller value means higher priority
    }

    void handle(ConsumingDomainEvent<T> consumingDomainEvent);
}
