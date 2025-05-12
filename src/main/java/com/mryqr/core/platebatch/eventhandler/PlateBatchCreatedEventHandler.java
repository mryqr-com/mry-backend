package com.mryqr.core.platebatch.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.plate.domain.task.CountPlateForTenantTask;
import com.mryqr.core.platebatch.domain.event.PlateBatchCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlateBatchCreatedEventHandler extends AbstractDomainEventHandler<PlateBatchCreatedEvent> {
    private final CountPlateForTenantTask countPlateForTenantTask;

    @Override
    protected void doHandle(PlateBatchCreatedEvent event) {
        MryTaskRunner.run(() -> countPlateForTenantTask.run(event.getArTenantId()));

    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
