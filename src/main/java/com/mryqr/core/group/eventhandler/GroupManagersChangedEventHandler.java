package com.mryqr.core.group.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.event.GroupManagersChangedEvent;
import com.mryqr.core.qr.domain.task.SyncGroupManagerAttributesForAllQrsUnderGroupTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.GROUP_MANAGERS_CHANGED;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupManagersChangedEventHandler implements DomainEventHandler {
    private final SyncGroupManagerAttributesForAllQrsUnderGroupTask syncGroupManagerAttributesForAllQrsUnderGroupTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == GROUP_MANAGERS_CHANGED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        GroupManagersChangedEvent event = (GroupManagersChangedEvent) domainEvent;
        taskRunner.run(() -> syncGroupManagerAttributesForAllQrsUnderGroupTask.run(event.getGroupId()));
    }
}
