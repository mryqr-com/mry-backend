package com.mryqr.core.assignmentplan.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.assignment.domain.task.RemoveAllAssignmentsUnderAssignmentPlanTask;
import com.mryqr.core.assignmentplan.domain.event.AssignmentPlanDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssignmentPlanDeletedEventHandler extends AbstractDomainEventHandler<AssignmentPlanDeletedEvent> {
    private final RemoveAllAssignmentsUnderAssignmentPlanTask removeAllAssignmentsUnderAssignmentPlanTask;

    @Override
    protected void doHandle(AssignmentPlanDeletedEvent event) {
        MryTaskRunner.run(() -> removeAllAssignmentsUnderAssignmentPlanTask.run(event.getAssignmentPlanId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
