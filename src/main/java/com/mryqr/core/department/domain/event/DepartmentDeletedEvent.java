package com.mryqr.core.department.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.DEPARTMENT_DELETED;
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
