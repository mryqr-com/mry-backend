package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.plate.domain.task.CountPlateForTenantTask;
import com.mryqr.core.qr.domain.QrCreatedEvent;
import com.mryqr.core.qr.domain.task.CountQrForAppTask;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCreatedEventHandler extends AbstractDomainEventHandler<QrCreatedEvent> {
    private final CountQrForAppTask countQrForAppTask;
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;
    private final CountPlateForTenantTask countPlateForTenantTask;

    @Override
    protected void doHandle(QrCreatedEvent event) {
        MryTaskRunner.run(() -> countQrForAppTask.run(event.getAppId(), event.getArTenantId()));
        MryTaskRunner.run(() -> countPlateForTenantTask.run(event.getArTenantId()));
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
