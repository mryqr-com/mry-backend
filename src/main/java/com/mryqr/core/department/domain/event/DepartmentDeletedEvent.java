package com.mryqr.core.department.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.DEPARTMENT_DELETED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("DEPARTMENT_DELETED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class DepartmentDeletedEvent extends DomainEvent {
    private String departmentId;

    public DepartmentDeletedEvent(String departmentId, User user) {
        super(DEPARTMENT_DELETED, user);
        this.departmentId = departmentId;
    }
}
