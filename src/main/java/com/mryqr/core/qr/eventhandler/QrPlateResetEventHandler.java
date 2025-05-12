package com.mryqr.core.qr.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrPlateResetEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import com.mryqr.core.submission.domain.task.SyncSubmissionPlateFromQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_PLATE_ID;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_PLATE_RESET;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrPlateResetEventHandler implements DomainEventHandler {
    private final SyncSubmissionPlateFromQrTask syncSubmissionPlateFromQrTask;
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == QR_PLATE_RESET;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        QrPlateResetEvent event = (QrPlateResetEvent) domainEvent;
        taskRunner.run(() -> syncSubmissionPlateFromQrTask.run(event.getQrId()));
        taskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_PLATE_ID));
    }

}
