package com.mryqr.core.submission.eventhandler;

import com.mryqr.common.event.consume.DomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.task.SyncSubmissionAwareAttributeValuesForQrTask;
import com.mryqr.core.submission.domain.event.SubmissionUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionUpdatedEventHandler extends DomainEventHandler<SubmissionUpdatedEvent> {
    private final SyncSubmissionAwareAttributeValuesForQrTask syncSubmissionAwareAttributesTask;

    @Override
    public void handle(SubmissionUpdatedEvent event) {
        MryTaskRunner.run(() -> syncSubmissionAwareAttributesTask.run(event.getQrId(), event.getPageId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
