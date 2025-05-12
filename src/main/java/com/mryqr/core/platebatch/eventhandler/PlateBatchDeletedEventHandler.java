package com.mryqr.core.platebatch.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.plate.domain.task.UnsetAllPlatesFromPlateBatchTask;
import com.mryqr.core.platebatch.domain.event.PlateBatchDeletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlateBatchDeletedEventHandler extends AbstractDomainEventHandler<PlateBatchDeletedEvent> {
    private final UnsetAllPlatesFromPlateBatchTask unsetAllPlatesFromPlateBatchTask;

    @Override
    protected void doHandle(PlateBatchDeletedEvent event) {
        MryTaskRunner.run(() -> unsetAllPlatesFromPlateBatchTask.run(event.getBatchId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
