package com.mryqr.core.assignment.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.ASSIGNMENT_FINISHED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("ASSIGNMENT_FINISHED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AssignmentFinishedEvent extends DomainEvent {
    private String assignmentId;

    public AssignmentFinishedEvent(String assignmentId, User user) {
        super(ASSIGNMENT_FINISHED, user);
        this.assignmentId = assignmentId;
    }
}
