package com.mryqr.core.department.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.DEPARTMENT_MANAGERS_CHANGED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("DEPARTMENT_MANAGERS_CHANGED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class DepartmentManagersChangedEvent extends DomainEvent {
    private String departmentId;

    public DepartmentManagersChangedEvent(String departmentId, User user) {
        super(DEPARTMENT_MANAGERS_CHANGED, user);
        this.departmentId = departmentId;
    }
}
