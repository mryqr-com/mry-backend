package com.mryqr.core.qr.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.event.QrMarkedAsTemplateEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import com.mryqr.core.qr.domain.task.SyncSubmissionAwareAttributeValuesForQrTask;
import com.mryqr.core.submission.domain.task.CountSubmissionForAppTask;
import com.mryqr.core.submission.domain.task.RemoveAllSubmissionsForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_TEMPLATE_STATUS;
import static com.mryqr.core.common.domain.event.DomainEventType.QR_MARKED_AS_TEMPLATE;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrMarkedAsTemplateEventHandler implements DomainEventHandler {
    private final QrRepository qrRepository;
    private final RemoveAllSubmissionsForQrTask removeAllSubmissionsForQrTask;
    private final SyncSubmissionAwareAttributeValuesForQrTask syncSubmissionAwareAttributeValuesForQrTask;
    private final CountSubmissionForAppTask countSubmissionForAppTask;
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == QR_MARKED_AS_TEMPLATE;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        QrMarkedAsTemplateEvent event = (QrMarkedAsTemplateEvent) domainEvent;

        qrRepository.byIdOptional(event.getQrId()).ifPresent(qr -> {
            if (qr.isTemplate()) {
                taskRunner.run(() -> removeAllSubmissionsForQrTask.run(qr.getId()));

                //须在removeAllSubmissionsForQrTask之后执行
                taskRunner.run(() -> syncSubmissionAwareAttributeValuesForQrTask.run(event.getQrId()));
            }
        });

        taskRunner.run(() -> countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId()));
        taskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_TEMPLATE_STATUS));
    }
}
