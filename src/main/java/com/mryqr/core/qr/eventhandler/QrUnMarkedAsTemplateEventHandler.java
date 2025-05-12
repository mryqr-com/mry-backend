package com.mryqr.core.qr.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrUnMarkedAsTemplateEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_TEMPLATE_STATUS;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_UNMARKED_AS_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrUnMarkedAsTemplateEventHandler implements DomainEventHandler {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == QR_UNMARKED_AS_TEMPLATE;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        QrUnMarkedAsTemplateEvent event = (QrUnMarkedAsTemplateEvent) domainEvent;
        taskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_TEMPLATE_STATUS));
    }
}
