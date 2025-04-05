package com.mryqr.core.app.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.event.AppPagesDeletedEvent;
import com.mryqr.core.assignment.domain.task.RemoveAllAssignmentsForPageTask;
import com.mryqr.core.assignmentplan.domain.task.RemoveAllAssignmentPlansForPageTask;
import com.mryqr.core.submission.domain.task.CountSubmissionForAppTask;
import com.mryqr.core.submission.domain.task.RemoveAllSubmissionsForPageTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppPagesDeletedEventHandler extends AbstractDomainEventHandler<AppPagesDeletedEvent> {
    private final RemoveAllSubmissionsForPageTask removeAllSubmissionsForPageTask;
    private final CountSubmissionForAppTask countSubmissionForAppTask;
    private final RemoveAllAssignmentPlansForPageTask removeAllAssignmentPlansForPageTask;
    private final RemoveAllAssignmentsForPageTask removeAllAssignmentsForPageTask;

    @Override
    public void handle(AppPagesDeletedEvent event) {
        String appId = event.getAppId();
        event.getPages().forEach(page -> {
            MryTaskRunner.run(() -> removeAllSubmissionsForPageTask.run(page.getPageId(), appId));
            MryTaskRunner.run(() -> removeAllAssignmentPlansForPageTask.run(page.getPageId(), appId));
            MryTaskRunner.run(() -> removeAllAssignmentsForPageTask.run(page.getPageId(), appId));
        });

        MryTaskRunner.run(() -> countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
