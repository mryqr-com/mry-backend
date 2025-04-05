package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.DomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrCirculationStatusChangedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_CIRCULATION_STATUS;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCirculationStatusChangedEventHandler extends DomainEventHandler<QrCirculationStatusChangedEvent> {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public void handle(QrCirculationStatusChangedEvent event) {
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_CIRCULATION_STATUS));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
