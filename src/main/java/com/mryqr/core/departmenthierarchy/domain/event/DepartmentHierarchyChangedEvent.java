package com.mryqr.core.departmenthierarchy.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.DEPARTMENT_HIERARCHY_CHANGED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("DEPARTMENT_HIERARCHY_CHANGED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class DepartmentHierarchyChangedEvent extends DomainEvent {
    private String tenantId;

    public DepartmentHierarchyChangedEvent(String tenantId, User user) {
        super(DEPARTMENT_HIERARCHY_CHANGED, user);
        this.tenantId = tenantId;
    }
}
