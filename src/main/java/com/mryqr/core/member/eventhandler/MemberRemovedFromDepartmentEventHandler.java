package com.mryqr.core.member.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.task.SyncDepartmentMembersToGroupTask;
import com.mryqr.core.member.domain.event.MemberRemovedFromDepartmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.MEMBER_REMOVED_FROM_DEPARTMENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberRemovedFromDepartmentEventHandler implements DomainEventHandler {
    private final SyncDepartmentMembersToGroupTask syncDepartmentMembersToGroupTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == MEMBER_REMOVED_FROM_DEPARTMENT;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        MemberRemovedFromDepartmentEvent event = (MemberRemovedFromDepartmentEvent) domainEvent;
        taskRunner.run(() -> syncDepartmentMembersToGroupTask.run(event.getDepartmentId()));
    }

}
