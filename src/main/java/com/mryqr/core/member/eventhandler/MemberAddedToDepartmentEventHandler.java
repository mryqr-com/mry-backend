package com.mryqr.core.member.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.task.SyncDepartmentMembersToGroupTask;
import com.mryqr.core.member.domain.event.MemberAddedToDepartmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberAddedToDepartmentEventHandler extends AbstractDomainEventHandler<MemberAddedToDepartmentEvent> {
    private final SyncDepartmentMembersToGroupTask syncDepartmentMembersToGroupTask;

    @Override
    protected void doHandle(MemberAddedToDepartmentEvent event) {
        MryTaskRunner.run(() -> syncDepartmentMembersToGroupTask.run(event.getDepartmentId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
