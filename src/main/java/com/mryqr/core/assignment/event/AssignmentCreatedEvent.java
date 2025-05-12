package com.mryqr.core.assignment.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.ASSIGNMENT_CREATED;
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
