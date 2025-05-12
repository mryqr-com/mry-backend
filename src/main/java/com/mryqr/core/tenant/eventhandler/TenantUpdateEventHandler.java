package com.mryqr.core.tenant.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.tenant.domain.event.TenantUpdatedEvent;
import com.mryqr.core.tenant.domain.task.SyncTenantToManagedQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantUpdateEventHandler implements DomainEventHandler {
    private final SyncTenantToManagedQrTask syncTenantToManagedQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent instanceof TenantUpdatedEvent;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        TenantUpdatedEvent theEvent = (TenantUpdatedEvent) domainEvent;
        taskRunner.run(() -> syncTenantToManagedQrTask.sync(theEvent.getTenantId()));
    }
}
