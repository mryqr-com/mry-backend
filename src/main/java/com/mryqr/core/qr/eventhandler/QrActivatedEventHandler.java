package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrActivatedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_ACTIVE_STATUS;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrActivatedEventHandler extends AbstractDomainEventHandler<QrActivatedEvent> {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    protected void doHandle(QrActivatedEvent event) {
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_ACTIVE_STATUS));

    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
