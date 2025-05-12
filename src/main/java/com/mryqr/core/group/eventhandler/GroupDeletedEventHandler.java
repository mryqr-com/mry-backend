package com.mryqr.core.group.eventhandler;

import com.mryqr.core.assignment.domain.task.RemoveAllAssignmentsUnderGroupTask;
import com.mryqr.core.assignmentplan.domain.task.RemoveGroupFromAllAssignmentPlansTask;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.event.GroupDeletedEvent;
import com.mryqr.core.group.domain.task.CountGroupForAppTask;
import com.mryqr.core.group.domain.task.DeltaCountGroupForAppTask;
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

import static com.mryqr.core.common.domain.event.DomainEventType.GROUP_DELETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupDeletedEventHandler implements DomainEventHandler {
    private final CountUsedPlatesForPlateBatchTask countUsedPlatesForPlateBatchTask;
    private final RemoveAllQrsUnderGroupTask removeAllQrsUnderGroupTask;
    private final RemoveAllSubmissionsForGroupTask removeAllSubmissionsForGroupTask;
    private final UnbindAllPlatesUnderGroupTask unbindAllPlatesUnderGroupTask;
    private final CountGroupForAppTask countGroupForAppTask;
    private final PlateRepository plateRepository;
    private final CountSubmissionForAppTask countSubmissionForAppTask;
    private final DeltaCountGroupForAppTask deltaCountGroupForAppTask;
    private final RemoveGroupFromAllAssignmentPlansTask removeGroupFromAllAssignmentPlansTask;
    private final RemoveAllAssignmentsUnderGroupTask removeAllAssignmentsUnderGroupTask;
    private final CountQrForAppTask countQrForAppTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == GROUP_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        GroupDeletedEvent event = (GroupDeletedEvent) domainEvent;
        String groupId = event.getGroupId();

        Set<String> affectedPlateBatchIds = plateRepository.allPlateBatchIdsReferencingGroup(groupId);
        taskRunner.run(() -> removeAllQrsUnderGroupTask.run(groupId));
        taskRunner.run(() -> removeAllSubmissionsForGroupTask.run(groupId));
        taskRunner.run(() -> unbindAllPlatesUnderGroupTask.run(groupId));
        taskRunner.run(() -> removeGroupFromAllAssignmentPlansTask.run(groupId, event.getAppId()));
        taskRunner.run(() -> removeAllAssignmentsUnderGroupTask.run(groupId));

        if (event.isNotConsumedBefore()) {
            taskRunner.run(() -> deltaCountGroupForAppTask.delta(event.getAppId(), event.getArTenantId(), -1));
        } else {
            taskRunner.run(() -> countGroupForAppTask.run(event.getAppId(), event.getArTenantId()));
        }

        taskRunner.run(() -> countQrForAppTask.run(event.getAppId(), event.getArTenantId()));
        taskRunner.run(() -> countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId()));
        affectedPlateBatchIds.forEach(plateBatchId -> taskRunner.run(() -> countUsedPlatesForPlateBatchTask.run(plateBatchId)));
    }

}
