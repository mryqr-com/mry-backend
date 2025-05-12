package com.mryqr.core.app.eventhandler;

import com.mryqr.core.app.domain.event.AppCreatedEvent;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.tenant.domain.task.CountAppForTenantTask;
import com.mryqr.core.tenant.domain.task.DeltaCountAppForTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.APP_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppCreatedEventHandler implements DomainEventHandler {
    private final CountAppForTenantTask countAppForTenantTask;
    private final DeltaCountAppForTenantTask deltaCountAppForTenantTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == APP_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        AppCreatedEvent event = (AppCreatedEvent) domainEvent;

        if (event.isNotConsumedBefore()) {
            taskRunner.run(() -> deltaCountAppForTenantTask.delta(event.getArTenantId(), 1));
        } else {
            taskRunner.run(() -> countAppForTenantTask.run(event.getArTenantId()));
        }
    }
}
