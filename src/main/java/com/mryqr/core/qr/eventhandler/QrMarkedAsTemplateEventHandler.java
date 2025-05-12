package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class QrMarkedAsTemplateEventHandler extends AbstractDomainEventHandler<QrMarkedAsTemplateEvent> {
    private final QrRepository qrRepository;
    private final RemoveAllSubmissionsForQrTask removeAllSubmissionsForQrTask;
    private final SyncSubmissionAwareAttributeValuesForQrTask syncSubmissionAwareAttributeValuesForQrTask;
    private final CountSubmissionForAppTask countSubmissionForAppTask;
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    protected void doHandle(QrMarkedAsTemplateEvent event) {
        qrRepository.byIdOptional(event.getQrId()).ifPresent(qr -> {
            if (qr.isTemplate()) {
                MryTaskRunner.run(() -> removeAllSubmissionsForQrTask.run(qr.getId()));

                //须在removeAllSubmissionsForQrTask之后执行
                MryTaskRunner.run(() -> syncSubmissionAwareAttributeValuesForQrTask.run(event.getQrId()));
            }
        });

        MryTaskRunner.run(() -> countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId()));
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId(), INSTANCE_TEMPLATE_STATUS));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
