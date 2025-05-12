package com.mryqr.core.qr.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrCustomIdUpdatedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CUSTOM_ID;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_CUSTOM_ID_UPDATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCustomIdUpdatedEventHandler implements DomainEventHandler {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == QR_CUSTOM_ID_UPDATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        QrCustomIdUpdatedEvent theEvent = (QrCustomIdUpdatedEvent) domainEvent;
        taskRunner.run(() -> syncAttributeValuesForQrTask.run(theEvent.getQrId(), INSTANCE_CUSTOM_ID));
    }
}
