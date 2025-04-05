package com.mryqr.core.department.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.department.domain.event.DepartmentCreatedEvent;
import com.mryqr.core.department.domain.task.CountDepartmentForTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentCreatedEventHandler extends AbstractDomainEventHandler<DepartmentCreatedEvent> {
    private final CountDepartmentForTenantTask countDepartmentForTenantTask;

    @Override
    public void handle(DepartmentCreatedEvent event) {
        MryTaskRunner.run(() -> countDepartmentForTenantTask.run(event.getArTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
