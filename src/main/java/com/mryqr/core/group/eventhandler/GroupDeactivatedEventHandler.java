package com.mryqr.core.group.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.event.GroupDeactivatedEvent;
import com.mryqr.core.qr.domain.task.SyncGroupActiveStatusToQrsTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupDeactivatedEventHandler extends AbstractDomainEventHandler<GroupDeactivatedEvent> {
    private final SyncGroupActiveStatusToQrsTask syncGroupActiveStatusToQrsTask;

    @Override
    protected void doHandle(GroupDeactivatedEvent event) {
        MryTaskRunner.run(() -> syncGroupActiveStatusToQrsTask.run(event.getGroupId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
