package com.mryqr.core.member.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.group.domain.task.SyncDepartmentMembersToGroupTask;
import com.mryqr.core.member.domain.event.MemberRemovedFromDepartmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberRemovedFromDepartmentEventHandler extends AbstractDomainEventHandler<MemberRemovedFromDepartmentEvent> {
    private final SyncDepartmentMembersToGroupTask syncDepartmentMembersToGroupTask;

    @Override
    public void handle(MemberRemovedFromDepartmentEvent event) {
        MryTaskRunner.run(() -> syncDepartmentMembersToGroupTask.run(event.getDepartmentId()));

    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
