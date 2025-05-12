package com.mryqr.core.assignmentplan.eventhandler;

import com.mryqr.core.assignment.domain.task.RemoveAllAssignmentsUnderAssignmentPlanTask;
import com.mryqr.core.assignmentplan.domain.event.AssignmentPlanDeletedEvent;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.ASSIGNMENT_PLAN_DELETED;

@Component
@RequiredArgsConstructor
public class AssignmentPlanDeletedEventHandler implements DomainEventHandler {
    private final RemoveAllAssignmentsUnderAssignmentPlanTask removeAllAssignmentsUnderAssignmentPlanTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == ASSIGNMENT_PLAN_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        AssignmentPlanDeletedEvent theEvent = (AssignmentPlanDeletedEvent) domainEvent;
        taskRunner.run(() -> removeAllAssignmentsUnderAssignmentPlanTask.run(theEvent.getAssignmentPlanId()));
    }
}
