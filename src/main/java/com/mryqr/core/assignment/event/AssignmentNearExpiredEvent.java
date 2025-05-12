package com.mryqr.core.assignment.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.ASSIGNMENT_NEAR_EXPIRED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ASSIGNMENT_NEAR_EXPIRED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AssignmentNearExpiredEvent extends DomainEvent {
    private String assignmentId;

    public AssignmentNearExpiredEvent(String assignmentId, User user) {
        super(ASSIGNMENT_NEAR_EXPIRED, user);
        this.assignmentId = assignmentId;
    }
}
