package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrPlateResetEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import com.mryqr.core.submission.domain.task.SyncSubmissionPlateFromQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_PLATE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrPlateResetEventHandler extends AbstractDomainEventHandler<QrPlateResetEvent> {
    private final SyncSubmissionPlateFromQrTask syncSubmissionPlateFromQrTask;
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public void handle(QrPlateResetEvent event) {
        MryTaskRunner.run(() -> syncSubmissionPlateFromQrTask.run(event.getQrId()));
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_PLATE_ID));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
