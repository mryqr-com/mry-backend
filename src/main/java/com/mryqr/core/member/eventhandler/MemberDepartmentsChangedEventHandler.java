package com.mryqr.core.member.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.task.SyncDepartmentMembersToGroupTask;
import com.mryqr.core.member.domain.event.MemberDepartmentsChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.MEMBER_DEPARTMENTS_CHANGED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDepartmentsChangedEventHandler implements DomainEventHandler {
    private final SyncDepartmentMembersToGroupTask syncDepartmentMembersToGroupTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == MEMBER_DEPARTMENTS_CHANGED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        MemberDepartmentsChangedEvent theEvent = (MemberDepartmentsChangedEvent) domainEvent;
        theEvent.getAddedDepartmentIds().forEach(departmentId -> taskRunner.run(() -> syncDepartmentMembersToGroupTask.run(departmentId)));
        theEvent.getRemovedDepartmentIds().forEach(departmentId -> taskRunner.run(() -> syncDepartmentMembersToGroupTask.run(departmentId)));
    }

}
