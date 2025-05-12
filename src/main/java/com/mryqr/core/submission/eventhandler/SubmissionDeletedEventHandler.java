package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.task.SyncSubmissionAwareAttributeValuesForQrTask;
import com.mryqr.core.submission.domain.event.SubmissionDeletedEvent;
import com.mryqr.core.submission.domain.task.CountSubmissionForAppTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionDeletedEventHandler extends AbstractDomainEventHandler<SubmissionDeletedEvent> {
    private final SyncSubmissionAwareAttributeValuesForQrTask syncSubmissionAwareAttributesTask;
    private final CountSubmissionForAppTask countSubmissionForAppTask;

    @Override
    protected void doHandle(SubmissionDeletedEvent event) {
        MryTaskRunner.run(() -> syncSubmissionAwareAttributesTask.run(event.getQrId(), event.getPageId()));
        MryTaskRunner.run(() -> countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
