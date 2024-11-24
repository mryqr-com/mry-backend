package com.mryqr.core.plate.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.plate.domain.event.PlateBoundEvent;
import com.mryqr.core.platebatch.domain.task.CountUsedPlatesForPlateBatchTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.PLATE_BOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlateBoundEventHandler implements DomainEventHandler {
    private final PlateRepository plateRepository;
    private final CountUsedPlatesForPlateBatchTask countUsedPlatesForPlateBatchTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == PLATE_BOUND;
    }

    @Override
    public void handle(DomainEvent domainEvent) {
        PlateBoundEvent event = (PlateBoundEvent) domainEvent;
        plateRepository.byIdOptional(event.getPlateId())
                .filter(Plate::isBatched)
                .ifPresent(plate -> MryTaskRunner.run(() -> countUsedPlatesForPlateBatchTask.run(plate.getBatchId())));
    }
}
