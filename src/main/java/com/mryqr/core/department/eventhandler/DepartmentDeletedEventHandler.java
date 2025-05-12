package com.mryqr.core.department.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.department.domain.event.DepartmentDeletedEvent;
import com.mryqr.core.department.domain.task.CountDepartmentForTenantTask;
import com.mryqr.core.department.domain.task.DeltaCountDepartmentForTenantTask;
import com.mryqr.core.member.domain.task.RemoveDepartmentFromAllMembersTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.DEPARTMENT_DELETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentDeletedEventHandler implements DomainEventHandler {
    private final CountDepartmentForTenantTask countDepartmentForTenantTask;
    private final DeltaCountDepartmentForTenantTask deltaCountDepartmentForTenantTask;
    private final RemoveDepartmentFromAllMembersTask removeDepartmentFromAllMembersTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == DEPARTMENT_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        DepartmentDeletedEvent event = (DepartmentDeletedEvent) domainEvent;

        if (event.isNotConsumedBefore()) {
            taskRunner.run(() -> deltaCountDepartmentForTenantTask.delta(event.getArTenantId(), -1));
        } else {
            taskRunner.run(() -> countDepartmentForTenantTask.run(event.getArTenantId()));
        }

        taskRunner.run(() -> removeDepartmentFromAllMembersTask.run(event.getDepartmentId(), event.getArTenantId()));
    }
}
