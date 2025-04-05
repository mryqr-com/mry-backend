package com.mryqr.core.group.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.event.GroupCreatedEvent;
import com.mryqr.core.group.domain.task.CountGroupForAppTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GroupCreatedEventHandler extends AbstractDomainEventHandler<GroupCreatedEvent> {
    private final CountGroupForAppTask countGroupForAppTask;

    @Override
    public void handle(GroupCreatedEvent event) {
        MryTaskRunner.run(() -> countGroupForAppTask.run(event.getAppId(), event.getArTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
