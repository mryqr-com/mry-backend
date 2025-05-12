package com.mryqr.core.tenant.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.tenant.domain.event.TenantUpdatedEvent;
import com.mryqr.core.tenant.domain.task.SyncTenantToManagedQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantUpdateEventHandler extends AbstractDomainEventHandler<TenantUpdatedEvent> {
    private final SyncTenantToManagedQrTask syncTenantToManagedQrTask;

    @Override
    protected void doHandle(TenantUpdatedEvent event) {
        MryTaskRunner.run(() -> syncTenantToManagedQrTask.sync(event.getTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
