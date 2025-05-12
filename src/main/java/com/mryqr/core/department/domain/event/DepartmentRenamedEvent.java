package com.mryqr.core.department.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.DEPARTMENT_RENAMED;
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
