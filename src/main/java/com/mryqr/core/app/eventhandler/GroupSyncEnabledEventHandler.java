package com.mryqr.core.app.eventhandler;

import com.mryqr.core.app.domain.event.GroupSyncEnabledEvent;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.task.SyncAllDepartmentsToGroupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.GROUP_SYNC_ENABLED;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupSyncEnabledEventHandler implements DomainEventHandler {
    private final SyncAllDepartmentsToGroupTask syncAllDepartmentsToGroupTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == GROUP_SYNC_ENABLED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        GroupSyncEnabledEvent theEvent = (GroupSyncEnabledEvent) domainEvent;
        taskRunner.run(() -> syncAllDepartmentsToGroupTask.run(theEvent.getAppId()));
    }
}
