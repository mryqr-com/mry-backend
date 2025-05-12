package com.mryqr.core.tenant.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.tenant.domain.event.TenantCreatedEvent;
import com.mryqr.core.tenant.domain.task.SyncTenantToManagedQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantCreatedEventHandler implements DomainEventHandler {
    private final SyncTenantToManagedQrTask syncTenantToManagedQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == TENANT_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        TenantCreatedEvent theEvent = (TenantCreatedEvent) domainEvent;
        taskRunner.run(() -> syncTenantToManagedQrTask.sync(theEvent.getTenantId()));
    }
}
