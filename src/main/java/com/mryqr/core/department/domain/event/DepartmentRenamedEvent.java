package com.mryqr.core.department.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.DEPARTMENT_RENAMED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("DEPARTMENT_RENAMED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class DepartmentRenamedEvent extends DomainEvent {
    private String departmentId;

    public DepartmentRenamedEvent(String departmentId, User user) {
        super(DEPARTMENT_RENAMED, user);
        this.departmentId = departmentId;
    }
}
