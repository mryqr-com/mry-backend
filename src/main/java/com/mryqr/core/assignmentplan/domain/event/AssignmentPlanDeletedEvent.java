package com.mryqr.core.assignmentplan.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.ASSIGNMENT_PLAN_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ASSIGNMENT_PLAN_DELETED")
@NoArgsConstructor(access = PRIVATE)
public class AssignmentPlanDeletedEvent extends DomainEvent {
    private String assignmentPlanId;

    public AssignmentPlanDeletedEvent(String assignmentPlanId, User user) {
        super(ASSIGNMENT_PLAN_DELETED, user);
        this.assignmentPlanId = assignmentPlanId;
    }
}
