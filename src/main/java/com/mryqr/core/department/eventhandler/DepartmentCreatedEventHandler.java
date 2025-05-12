package com.mryqr.core.department.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.department.domain.event.DepartmentCreatedEvent;
import com.mryqr.core.department.domain.task.CountDepartmentForTenantTask;
import com.mryqr.core.department.domain.task.DeltaCountDepartmentForTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.DEPARTMENT_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentCreatedEventHandler implements DomainEventHandler {
    private final CountDepartmentForTenantTask countDepartmentForTenantTask;
    private final DeltaCountDepartmentForTenantTask deltaCountDepartmentForTenantTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == DEPARTMENT_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        DepartmentCreatedEvent event = (DepartmentCreatedEvent) domainEvent;

        if (event.isNotConsumedBefore()) {
            taskRunner.run(() -> deltaCountDepartmentForTenantTask.delta(event.getArTenantId(), 1));
        } else {
            taskRunner.run(() -> countDepartmentForTenantTask.run(event.getArTenantId()));
        }
    }

}
