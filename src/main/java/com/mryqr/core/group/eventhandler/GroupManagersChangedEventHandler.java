package com.mryqr.core.group.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.event.GroupManagersChangedEvent;
import com.mryqr.core.qr.domain.task.SyncGroupManagerAttributesForAllQrsUnderGroupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupManagersChangedEventHandler extends AbstractDomainEventHandler<GroupManagersChangedEvent> {
    private final SyncGroupManagerAttributesForAllQrsUnderGroupTask syncGroupManagerAttributesForAllQrsUnderGroupTask;

    @Override
    protected void doHandle(GroupManagersChangedEvent event) {
        MryTaskRunner.run(() -> syncGroupManagerAttributesForAllQrsUnderGroupTask.run(event.getGroupId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
