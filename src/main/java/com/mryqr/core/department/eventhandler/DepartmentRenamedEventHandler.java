package com.mryqr.core.department.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.department.domain.event.DepartmentRenamedEvent;
import com.mryqr.core.group.domain.task.SyncDepartmentToGroupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.DEPARTMENT_RENAMED;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentRenamedEventHandler implements DomainEventHandler {
    private final SyncDepartmentToGroupTask syncDepartmentToGroupTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == DEPARTMENT_RENAMED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        DepartmentRenamedEvent theEvent = (DepartmentRenamedEvent) domainEvent;
        taskRunner.run(() -> syncDepartmentToGroupTask.run(theEvent.getDepartmentId()));
    }

}
