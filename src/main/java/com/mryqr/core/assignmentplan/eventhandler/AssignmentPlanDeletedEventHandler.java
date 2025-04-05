package com.mryqr.core.assignmentplan.eventhandler;

import com.mryqr.common.event.consume.DomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.assignment.domain.task.RemoveAllAssignmentsUnderAssignmentPlanTask;
import com.mryqr.core.assignmentplan.domain.event.AssignmentPlanDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentPlanDeletedEventHandler extends DomainEventHandler<AssignmentPlanDeletedEvent> {
    private final RemoveAllAssignmentsUnderAssignmentPlanTask removeAllAssignmentsUnderAssignmentPlanTask;

    @Override
    public void handle(AssignmentPlanDeletedEvent event) {
        MryTaskRunner.run(() -> removeAllAssignmentsUnderAssignmentPlanTask.run(event.getAssignmentPlanId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
