package com.mryqr.core.plate.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.plate.domain.event.PlateUnboundEvent;
import com.mryqr.core.platebatch.domain.task.CountUsedPlatesForPlateBatchTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlateUnboundEventHandler extends AbstractDomainEventHandler<PlateUnboundEvent> {
    private final PlateRepository plateRepository;
    private final CountUsedPlatesForPlateBatchTask countUsedPlatesForPlateBatchTask;

    @Override
    protected void doHandle(PlateUnboundEvent event) {
        plateRepository.byIdOptional(event.getPlateId())
                .filter(Plate::isBatched)
                .ifPresent(plate -> MryTaskRunner.run(() -> countUsedPlatesForPlateBatchTask.run(plate.getBatchId())));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
