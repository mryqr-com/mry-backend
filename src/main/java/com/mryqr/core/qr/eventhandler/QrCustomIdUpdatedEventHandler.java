package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrCustomIdUpdatedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CUSTOM_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCustomIdUpdatedEventHandler extends AbstractDomainEventHandler<QrCustomIdUpdatedEvent> {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public void handle(QrCustomIdUpdatedEvent event) {
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_CUSTOM_ID));

    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
