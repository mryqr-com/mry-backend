package com.mryqr.core.department.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.department.domain.event.DepartmentDeletedEvent;
import com.mryqr.core.department.domain.task.CountDepartmentForTenantTask;
import com.mryqr.core.member.domain.task.RemoveDepartmentFromAllMembersTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentDeletedEventHandler extends AbstractDomainEventHandler<DepartmentDeletedEvent> {
    private final CountDepartmentForTenantTask countDepartmentForTenantTask;
    private final RemoveDepartmentFromAllMembersTask removeDepartmentFromAllMembersTask;

    @Override
    protected void doHandle(DepartmentDeletedEvent event) {
        MryTaskRunner.run(() -> countDepartmentForTenantTask.run(event.getArTenantId()));
        MryTaskRunner.run(() -> removeDepartmentFromAllMembersTask.run(event.getDepartmentId(), event.getArTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }

}
