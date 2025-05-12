package com.mryqr.core.member.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.task.SyncDepartmentMembersToGroupTask;
import com.mryqr.core.member.domain.event.MemberDepartmentsChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDepartmentsChangedEventHandler extends AbstractDomainEventHandler<MemberDepartmentsChangedEvent> {
    private final SyncDepartmentMembersToGroupTask syncDepartmentMembersToGroupTask;

    @Override
    public void handle(MemberDepartmentsChangedEvent event) {
        event.getAddedDepartmentIds().forEach(departmentId -> MryTaskRunner.run(() -> syncDepartmentMembersToGroupTask.run(departmentId)));
        event.getRemovedDepartmentIds().forEach(departmentId -> MryTaskRunner.run(() -> syncDepartmentMembersToGroupTask.run(departmentId)));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
