package com.mryqr.core.assignment.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.ASSIGNMENT_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ASSIGNMENT_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AssignmentCreatedEvent extends DomainEvent {
    private String assignmentId;

    public AssignmentCreatedEvent(String assignmentId, User user) {
        super(ASSIGNMENT_CREATED, user);
        this.assignmentId = assignmentId;
    }
}
