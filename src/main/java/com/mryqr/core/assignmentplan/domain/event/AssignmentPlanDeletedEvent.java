package com.mryqr.core.assignmentplan.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.ASSIGNMENT_PLAN_DELETED;
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
