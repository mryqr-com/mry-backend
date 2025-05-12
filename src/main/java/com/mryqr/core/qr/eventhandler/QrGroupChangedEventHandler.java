package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.plate.domain.task.SyncPlateGroupFromQrTask;
import com.mryqr.core.qr.domain.event.QrGroupChangedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import com.mryqr.core.qr.domain.task.SyncGroupActiveStatusToQrTask;
import com.mryqr.core.submission.domain.task.SyncSubmissionGroupFromQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrGroupChangedEventHandler extends AbstractDomainEventHandler<QrGroupChangedEvent> {
    private final SyncSubmissionGroupFromQrTask syncSubmissionGroupFromQrTask;
    private final SyncPlateGroupFromQrTask syncPlateGroupFromQrTask;
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;
    private final SyncGroupActiveStatusToQrTask syncGroupActiveStatusToQrTask;

    @Override
    public void handle(QrGroupChangedEvent event) {
        MryTaskRunner.run(() -> syncSubmissionGroupFromQrTask.run(event.getQrId()));
        MryTaskRunner.run(() -> syncGroupActiveStatusToQrTask.run(event.getQrId()));
        MryTaskRunner.run(() -> syncPlateGroupFromQrTask.run(event.getQrId()));
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(),
                INSTANCE_GROUP,
                INSTANCE_GROUP_MANAGERS,
                INSTANCE_GROUP_MANAGERS_AND_MOBILE,
                INSTANCE_GROUP_MANAGERS_AND_EMAIL));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
