package com.mryqr.core.group.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.event.GroupActivatedEvent;
import com.mryqr.core.qr.domain.task.SyncGroupActiveStatusToQrsTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupActivatedEventHandler extends AbstractDomainEventHandler<GroupActivatedEvent> {
    private final SyncGroupActiveStatusToQrsTask syncGroupActiveStatusToQrsTask;

    @Override
    public void handle(GroupActivatedEvent event) {
        MryTaskRunner.run(() -> syncGroupActiveStatusToQrsTask.run(event.getGroupId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
