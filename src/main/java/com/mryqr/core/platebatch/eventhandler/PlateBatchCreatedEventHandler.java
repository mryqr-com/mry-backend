package com.mryqr.core.platebatch.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.plate.domain.task.CountPlateForTenantTask;
import com.mryqr.core.plate.domain.task.DeltaCountPlateForTenantTask;
import com.mryqr.core.platebatch.domain.event.PlateBatchCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.PLATE_BATCH_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlateBatchCreatedEventHandler implements DomainEventHandler {
    private final CountPlateForTenantTask countPlateForTenantTask;
    private final DeltaCountPlateForTenantTask deltaCountPlateForTenantTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == PLATE_BATCH_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        PlateBatchCreatedEvent event = (PlateBatchCreatedEvent) domainEvent;

        if (event.isNotConsumedBefore()) {
            taskRunner.run(() -> deltaCountPlateForTenantTask.delta(event.getArTenantId(), event.getPlateCount()));
        } else {
            taskRunner.run(() -> countPlateForTenantTask.run(event.getArTenantId()));
        }
    }

}
