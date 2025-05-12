package com.mryqr.core.department.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.DEPARTMENT_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("DEPARTMENT_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class DepartmentCreatedEvent extends DomainEvent {
    private String departmentId;

    public DepartmentCreatedEvent(String departmentId, User user) {
        super(DEPARTMENT_CREATED, user);
        this.departmentId = departmentId;
    }
}
