package com.mryqr.core.assignment.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.ASSIGNMENT_FAILED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ASSIGNMENT_FAILED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AssignmentFailedEvent extends DomainEvent {
    private String assignmentId;

    public AssignmentFailedEvent(String assignmentId, User user) {
        super(ASSIGNMENT_FAILED, user);
        this.assignmentId = assignmentId;
    }
}
