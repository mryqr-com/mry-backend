package com.mryqr.core.qr.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrCirculationStatusChangedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CIRCULATION_STATUS;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_CIRCULATION_STATUS_CHANGED;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCirculationStatusChangedEventHandler implements DomainEventHandler {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == QR_CIRCULATION_STATUS_CHANGED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        QrCirculationStatusChangedEvent theEvent = (QrCirculationStatusChangedEvent) domainEvent;
        taskRunner.run(() -> syncAttributeValuesForQrTask.run(theEvent.getQrId(), INSTANCE_CIRCULATION_STATUS));
    }
}
