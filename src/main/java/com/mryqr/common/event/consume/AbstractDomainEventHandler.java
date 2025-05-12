package com.mryqr.common.event.consume;

public abstract class AbstractDomainEventHandler<T> {

    public boolean isIdempotent() {
        return false; // By default, all handlers are assumed to be not idempotent
    }

    public boolean isTransactional() {
        return false; // By default, all handlers are assumed to be not transactional
    }

    public int priority() {
        return 0; // Smaller value means higher priority
    }

    public abstract void handle(T domainEvent);
}
