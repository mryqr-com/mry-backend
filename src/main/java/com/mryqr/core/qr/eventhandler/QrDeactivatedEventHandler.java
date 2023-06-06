package com.mryqr.core.qr.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrDeactivatedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_ACTIVE_STATUS;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_DEACTIVATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrDeactivatedEventHandler implements DomainEventHandler {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == QR_DEACTIVATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        QrDeactivatedEvent theEvent = (QrDeactivatedEvent) domainEvent;
        taskRunner.run(() -> syncAttributeValuesForQrTask.run(theEvent.getQrId(), INSTANCE_ACTIVE_STATUS));
    }
}
