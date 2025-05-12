package com.mryqr.core.group.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.assignment.domain.task.RemoveAllAssignmentsUnderGroupTask;
import com.mryqr.core.assignmentplan.domain.task.RemoveGroupFromAllAssignmentPlansTask;
import com.mryqr.core.group.domain.event.GroupDeletedEvent;
import com.mryqr.core.group.domain.task.CountGroupForAppTask;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.plate.domain.task.UnbindAllPlatesUnderGroupTask;
import com.mryqr.core.platebatch.domain.task.CountUsedPlatesForPlateBatchTask;
import com.mryqr.core.qr.domain.task.CountQrForAppTask;
import com.mryqr.core.qr.domain.task.RemoveAllQrsUnderGroupTask;
import com.mryqr.core.submission.domain.task.CountSubmissionForAppTask;
import com.mryqr.core.submission.domain.task.RemoveAllSubmissionsForGroupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupDeletedEventHandler extends AbstractDomainEventHandler<GroupDeletedEvent> {
    private final CountUsedPlatesForPlateBatchTask countUsedPlatesForPlateBatchTask;
    private final RemoveAllQrsUnderGroupTask removeAllQrsUnderGroupTask;
    private final RemoveAllSubmissionsForGroupTask removeAllSubmissionsForGroupTask;
    private final UnbindAllPlatesUnderGroupTask unbindAllPlatesUnderGroupTask;
    private final CountGroupForAppTask countGroupForAppTask;
    private final PlateRepository plateRepository;
    private final CountSubmissionForAppTask countSubmissionForAppTask;
    private final RemoveGroupFromAllAssignmentPlansTask removeGroupFromAllAssignmentPlansTask;
    private final RemoveAllAssignmentsUnderGroupTask removeAllAssignmentsUnderGroupTask;
    private final CountQrForAppTask countQrForAppTask;

    @Override
    public void handle(GroupDeletedEvent event) {
        String groupId = event.getGroupId();

        Set<String> affectedPlateBatchIds = plateRepository.allPlateBatchIdsReferencingGroup(groupId);
        MryTaskRunner.run(() -> removeAllQrsUnderGroupTask.run(groupId));
        MryTaskRunner.run(() -> removeAllSubmissionsForGroupTask.run(groupId));
        MryTaskRunner.run(() -> unbindAllPlatesUnderGroupTask.run(groupId));
        MryTaskRunner.run(() -> removeGroupFromAllAssignmentPlansTask.run(groupId, event.getAppId()));
        MryTaskRunner.run(() -> removeAllAssignmentsUnderGroupTask.run(groupId));
        MryTaskRunner.run(() -> countGroupForAppTask.run(event.getAppId(), event.getArTenantId()));
        MryTaskRunner.run(() -> countQrForAppTask.run(event.getAppId(), event.getArTenantId()));
        MryTaskRunner.run(() -> countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId()));
        affectedPlateBatchIds.forEach(plateBatchId -> MryTaskRunner.run(() -> countUsedPlatesForPlateBatchTask.run(plateBatchId)));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
