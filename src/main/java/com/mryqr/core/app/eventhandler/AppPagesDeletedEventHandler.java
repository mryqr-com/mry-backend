package com.mryqr.core.app.eventhandler;

import com.mryqr.core.app.domain.event.AppPagesDeletedEvent;
import com.mryqr.core.assignment.domain.task.RemoveAllAssignmentsForPageTask;
import com.mryqr.core.assignmentplan.domain.task.RemoveAllAssignmentPlansForPageTask;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.submission.domain.task.CountSubmissionForAppTask;
import com.mryqr.core.submission.domain.task.RemoveAllSubmissionsForPageTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.PAGES_DELETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppPagesDeletedEventHandler implements DomainEventHandler {
    private final RemoveAllSubmissionsForPageTask removeAllSubmissionsForPageTask;
    private final CountSubmissionForAppTask countSubmissionForAppTask;
    private final RemoveAllAssignmentPlansForPageTask removeAllAssignmentPlansForPageTask;
    private final RemoveAllAssignmentsForPageTask removeAllAssignmentsForPageTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == PAGES_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        AppPagesDeletedEvent event = (AppPagesDeletedEvent) domainEvent;
        String appId = event.getAppId();
        event.getPages().forEach(page -> {
            taskRunner.run(() -> removeAllSubmissionsForPageTask.run(page.getPageId(), appId));
            taskRunner.run(() -> removeAllAssignmentPlansForPageTask.run(page.getPageId(), appId));
            taskRunner.run(() -> removeAllAssignmentsForPageTask.run(page.getPageId(), appId));
        });

        taskRunner.run(() -> countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId()));
    }
}
