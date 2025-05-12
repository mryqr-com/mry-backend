package com.mryqr.core.platebatch.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.plate.domain.task.UnsetAllPlatesFromPlateBatchTask;
import com.mryqr.core.platebatch.domain.event.PlateBatchDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.PLATE_BATCH_DELETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlateBatchDeletedEventHandler implements DomainEventHandler {
    private final UnsetAllPlatesFromPlateBatchTask unsetAllPlatesFromPlateBatchTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == PLATE_BATCH_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        PlateBatchDeletedEvent event = (PlateBatchDeletedEvent) domainEvent;
        taskRunner.run(() -> unsetAllPlatesFromPlateBatchTask.run(event.getBatchId()));
    }

}
