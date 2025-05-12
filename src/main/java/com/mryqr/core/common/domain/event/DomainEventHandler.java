package com.mryqr.core.common.domain.event;

import com.mryqr.core.common.utils.MryTaskRunner;

public interface DomainEventHandler {

    boolean canHandle(DomainEvent domainEvent);

    void handle(DomainEvent domainEvent, MryTaskRunner taskRunner);

    default int priority() {
        return 0;//越小优先级越高
    }

}
