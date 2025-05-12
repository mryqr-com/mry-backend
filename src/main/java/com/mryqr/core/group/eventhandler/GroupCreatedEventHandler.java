package com.mryqr.core.group.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.event.GroupCreatedEvent;
import com.mryqr.core.group.domain.task.CountGroupForAppTask;
import com.mryqr.core.group.domain.task.DeltaCountGroupForAppTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.GROUP_CREATED;

@Component
@RequiredArgsConstructor
public class GroupCreatedEventHandler implements DomainEventHandler {
    private final CountGroupForAppTask countGroupForAppTask;
    private final DeltaCountGroupForAppTask deltaCountGroupForAppTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == GROUP_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        GroupCreatedEvent event = (GroupCreatedEvent) domainEvent;

        if (event.isNotConsumedBefore()) {
            taskRunner.run(() -> deltaCountGroupForAppTask.delta(event.getAppId(), event.getArTenantId(), 1));
        } else {
            taskRunner.run(() -> countGroupForAppTask.run(event.getAppId(), event.getArTenantId()));
        }
    }
}
