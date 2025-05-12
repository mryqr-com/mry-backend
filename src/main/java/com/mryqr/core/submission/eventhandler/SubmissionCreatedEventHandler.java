package com.mryqr.core.submission.eventhandler;

import com.mryqr.core.assignment.domain.task.FinishQrForAssignmentsTask;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.task.SyncSubmissionAwareAttributeValuesForQrTask;
import com.mryqr.core.submission.domain.event.SubmissionCreatedEvent;
import com.mryqr.core.submission.domain.task.CountSubmissionForAppTask;
import com.mryqr.core.submission.domain.task.DeltaCountSubmissionForAppTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionCreatedEventHandler implements DomainEventHandler {
    private final SyncSubmissionAwareAttributeValuesForQrTask syncSubmissionAwareAttributesTask;
    private final CountSubmissionForAppTask countSubmissionForAppTask;
    private final DeltaCountSubmissionForAppTask deltaCountSubmissionForAppTask;
    private final FinishQrForAssignmentsTask finishQrForAssignmentsTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == SUBMISSION_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        SubmissionCreatedEvent event = (SubmissionCreatedEvent) domainEvent;
        taskRunner.run(() -> syncSubmissionAwareAttributesTask.run(event.getQrId(), event.getPageId()));

        if (event.isNotConsumedBefore()) {
            taskRunner.run(() -> deltaCountSubmissionForAppTask.delta(event.getAppId(), event.getArTenantId(), 1));
        } else {
            taskRunner.run(() -> countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId()));
        }

        taskRunner.run(() -> finishQrForAssignmentsTask.run(event.getQrId(),
                event.getSubmissionId(),
                event.getAppId(),
                event.getPageId(),
                event.getRaisedBy(),
                event.getRaisedAt()));
    }
}
