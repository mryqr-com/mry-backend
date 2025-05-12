package com.mryqr.core.app.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.event.AppGroupSyncEnabledEvent;
import com.mryqr.core.group.domain.task.SyncAllDepartmentsToGroupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupSyncEnabledEventHandler extends AbstractDomainEventHandler<AppGroupSyncEnabledEvent> {
    private final SyncAllDepartmentsToGroupTask syncAllDepartmentsToGroupTask;

    @Override
    protected void doHandle(AppGroupSyncEnabledEvent event) {
        MryTaskRunner.run(() -> syncAllDepartmentsToGroupTask.run(event.getAppId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
