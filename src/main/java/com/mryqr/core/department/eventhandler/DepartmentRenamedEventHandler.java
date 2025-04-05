package com.mryqr.core.department.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.department.domain.event.DepartmentRenamedEvent;
import com.mryqr.core.group.domain.task.SyncDepartmentToGroupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentRenamedEventHandler extends AbstractDomainEventHandler<DepartmentRenamedEvent> {
    private final SyncDepartmentToGroupTask syncDepartmentToGroupTask;

    @Override
    public void handle(DepartmentRenamedEvent event) {
        MryTaskRunner.run(() -> syncDepartmentToGroupTask.run(event.getDepartmentId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
