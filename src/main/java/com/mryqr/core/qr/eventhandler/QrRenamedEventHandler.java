package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrRenamedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_NAME;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrRenamedEventHandler extends AbstractDomainEventHandler<QrRenamedEvent> {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    protected void doHandle(QrRenamedEvent event) {
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_NAME));

    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
