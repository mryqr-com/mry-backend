package com.mryqr.core.submission.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.task.SyncSubmissionAwareAttributeValuesForQrTask;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.SUBMISSION_UPDATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionUpdatedEventHandler implements DomainEventHandler {
    private final SyncSubmissionAwareAttributeValuesForQrTask syncSubmissionAwareAttributesTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == SUBMISSION_UPDATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        SubmissionUpdatedEvent event = (SubmissionUpdatedEvent) domainEvent;
        taskRunner.run(() -> syncSubmissionAwareAttributesTask.run(event.getQrId(), event.getPageId()));
    }
}
