package com.mryqr.core.tenant.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.member.domain.task.SyncTenantActiveStatusToMembersTask;
import com.mryqr.core.tenant.domain.event.TenantActivatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_ACTIVATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantActivatedEventHandler implements DomainEventHandler {
    private final SyncTenantActiveStatusToMembersTask syncTenantActiveStatusToMembersTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == TENANT_ACTIVATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        TenantActivatedEvent theEvent = (TenantActivatedEvent) domainEvent;

        taskRunner.run(() -> syncTenantActiveStatusToMembersTask.run(theEvent.getTenantId()));
    }
}
