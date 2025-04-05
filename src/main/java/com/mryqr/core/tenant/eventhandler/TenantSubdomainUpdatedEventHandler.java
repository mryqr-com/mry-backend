package com.mryqr.core.tenant.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.core.tenant.domain.event.TenantSubdomainUpdatedEvent;
import com.mryqr.core.tenant.domain.task.SyncTenantSubdomainToAliyunTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantSubdomainUpdatedEventHandler extends AbstractDomainEventHandler<TenantSubdomainUpdatedEvent> {
    private final SyncTenantSubdomainToAliyunTask syncTenantSubdomainToAliyunTask;

    @Override
    public void handle(TenantSubdomainUpdatedEvent event) {
        syncTenantSubdomainToAliyunTask.run(event.getArTenantId());
    }
}
