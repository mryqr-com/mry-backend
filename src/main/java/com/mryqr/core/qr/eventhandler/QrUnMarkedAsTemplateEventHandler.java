package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrUnMarkedAsTemplateEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_TEMPLATE_STATUS;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrUnMarkedAsTemplateEventHandler extends AbstractDomainEventHandler<QrUnMarkedAsTemplateEvent> {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    protected void doHandle(QrUnMarkedAsTemplateEvent event) {
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_TEMPLATE_STATUS));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
