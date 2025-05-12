package com.mryqr.core.app.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.event.AppDeletedEvent;
import com.mryqr.core.appmanual.domain.task.RemoveManualForAppTask;
import com.mryqr.core.assignment.domain.task.RemoveAllAssignmentsUnderAppTask;
import com.mryqr.core.assignmentplan.domain.task.RemoveAllAssignmentPlansUnderAppTask;
import com.mryqr.core.group.domain.task.RemoveAllGroupsForAppTask;
import com.mryqr.core.grouphierarchy.domain.task.RemoveGroupHierarchyForAppTask;
import com.mryqr.core.plate.domain.task.RemoveAllPlatesUnderAppTask;
import com.mryqr.core.platebatch.domain.task.RemoveAllPlateBatchesUnderAppTask;
import com.mryqr.core.qr.domain.task.RemoveAllQrsUnderAppTask;
import com.mryqr.core.submission.domain.task.RemoveAllSubmissionsForAppTask;
import com.mryqr.core.tenant.domain.task.CountAppForTenantTask;
import com.mryqr.core.tenant.domain.task.RemoveAppUsageFromTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppDeletedEventHandler extends AbstractDomainEventHandler<AppDeletedEvent> {
    private final RemoveAllSubmissionsForAppTask removeAllSubmissionsForAppTask;
    private final RemoveAllQrsUnderAppTask removeAllQrsUnderAppTask;
    private final RemoveAllPlatesUnderAppTask removeAllPlatesUnderAppTask;
    private final RemoveAllPlateBatchesUnderAppTask removeAllPlateBatchesUnderAppTask;
    private final RemoveAppUsageFromTenantTask removeAppUsageFromTenantTask;
    private final RemoveManualForAppTask removeManualForAppTask;
    private final RemoveAllAssignmentPlansUnderAppTask removeAllAssignmentPlansUnderAppTask;
    private final RemoveAllAssignmentsUnderAppTask removeAllAssignmentsUnderAppTask;
    private final RemoveAllGroupsForAppTask removeAllGroupsForAppTask;
    private final RemoveGroupHierarchyForAppTask removeGroupHierarchyForAppTask;
    private final CountAppForTenantTask countAppForTenantTask;

    @Override
    protected void doHandle(AppDeletedEvent event) {
        String appId = event.getAppId();
        MryTaskRunner.run(() -> removeAllSubmissionsForAppTask.run(appId));
        MryTaskRunner.run(() -> removeAllQrsUnderAppTask.run(appId));
        MryTaskRunner.run(() -> removeAllGroupsForAppTask.run(appId));
        MryTaskRunner.run(() -> removeGroupHierarchyForAppTask.run(appId));
        MryTaskRunner.run(() -> removeAllPlatesUnderAppTask.run(appId));
        MryTaskRunner.run(() -> removeAllPlateBatchesUnderAppTask.run(appId));
        MryTaskRunner.run(() -> removeAppUsageFromTenantTask.run(event.getArTenantId(), appId));
        MryTaskRunner.run(() -> removeManualForAppTask.run(appId));
        MryTaskRunner.run(() -> removeAllAssignmentPlansUnderAppTask.run(appId));
        MryTaskRunner.run(() -> removeAllAssignmentsUnderAppTask.run(appId));
        MryTaskRunner.run(() -> countAppForTenantTask.run(event.getArTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
