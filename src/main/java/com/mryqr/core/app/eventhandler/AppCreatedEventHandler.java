package com.mryqr.core.app.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.event.AppCreatedEvent;
import com.mryqr.core.tenant.domain.task.CountAppForTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppCreatedEventHandler extends AbstractDomainEventHandler<AppCreatedEvent> {
    private final CountAppForTenantTask countAppForTenantTask;

    @Override
    public void handle(AppCreatedEvent event) {
        MryTaskRunner.run(() -> countAppForTenantTask.run(event.getArTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
