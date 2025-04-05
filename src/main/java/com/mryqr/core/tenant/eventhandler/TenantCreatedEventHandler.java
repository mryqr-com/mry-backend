package com.mryqr.core.tenant.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.tenant.domain.event.TenantCreatedEvent;
import com.mryqr.core.tenant.domain.task.SyncTenantToManagedQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantCreatedEventHandler extends AbstractDomainEventHandler<TenantCreatedEvent> {
    private final SyncTenantToManagedQrTask syncTenantToManagedQrTask;

    @Override
    public void handle(TenantCreatedEvent event) {
        MryTaskRunner.run(() -> syncTenantToManagedQrTask.sync(event.getTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
