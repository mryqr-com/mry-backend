package com.mryqr.core.plate.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.plate.domain.event.PlateUnboundEvent;
import com.mryqr.core.platebatch.domain.task.CountUsedPlatesForPlateBatchTask;
import com.mryqr.core.platebatch.domain.task.DeltaCountUsedPlatesForPlateBatchTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.PLATE_UNBOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlateUnboundEventHandler implements DomainEventHandler {
    private final PlateRepository plateRepository;
    private final CountUsedPlatesForPlateBatchTask countUsedPlatesForPlateBatchTask;
    private final DeltaCountUsedPlatesForPlateBatchTask deltaCountUsedPlatesForPlateBatchTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == PLATE_UNBOUND;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        PlateUnboundEvent event = (PlateUnboundEvent) domainEvent;
        plateRepository.byIdOptional(event.getPlateId())
                .filter(Plate::isBatched)
                .ifPresent(plate -> {
                    if (event.isNotConsumedBefore()) {
                        taskRunner.run(() -> deltaCountUsedPlatesForPlateBatchTask.delta(plate.getBatchId(), -1));
                    } else {
                        taskRunner.run(() -> countUsedPlatesForPlateBatchTask.run(plate.getBatchId()));
                    }
                });
    }
}
