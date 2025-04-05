package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.assignment.domain.task.FinishQrForAssignmentsTask;
import com.mryqr.core.qr.domain.task.SyncSubmissionAwareAttributeValuesForQrTask;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.submission.domain.task.CountSubmissionForAppTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionCreatedEventHandler extends AbstractDomainEventHandler<SubmissionCreatedEvent> {
    private final SyncSubmissionAwareAttributeValuesForQrTask syncSubmissionAwareAttributesTask;
    private final CountSubmissionForAppTask countSubmissionForAppTask;
    private final FinishQrForAssignmentsTask finishQrForAssignmentsTask;

    @Override
    public void handle(SubmissionCreatedEvent event) {
        MryTaskRunner.run(() -> syncSubmissionAwareAttributesTask.run(event.getQrId(), event.getPageId()));
        MryTaskRunner.run(() -> countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId()));
        MryTaskRunner.run(() -> finishQrForAssignmentsTask.run(event.getQrId(),
                event.getSubmissionId(),
                event.getAppId(),
                event.getPageId(),
                event.getRaisedBy(),
                event.getRaisedAt()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
