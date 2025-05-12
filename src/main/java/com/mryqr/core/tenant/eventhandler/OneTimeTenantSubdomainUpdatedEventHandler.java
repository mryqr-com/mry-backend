package com.mryqr.core.tenant.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.OneTimeDomainEventHandler;
import com.mryqr.core.tenant.domain.event.TenantSubdomainUpdatedEvent;
import com.mryqr.core.tenant.domain.task.SyncTenantSubdomainToAliyunTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_SUBDOMAIN_UPDATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class OneTimeTenantSubdomainUpdatedEventHandler extends OneTimeDomainEventHandler {
    private final SyncTenantSubdomainToAliyunTask syncTenantSubdomainToAliyunTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == TENANT_SUBDOMAIN_UPDATED;
    }

    @Override
    protected void doHandle(DomainEvent domainEvent) {
        TenantSubdomainUpdatedEvent event = (TenantSubdomainUpdatedEvent) domainEvent;

        syncTenantSubdomainToAliyunTask.run(event.getArTenantId());
    }
}
