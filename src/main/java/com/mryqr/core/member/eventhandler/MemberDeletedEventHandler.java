package com.mryqr.core.member.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.task.RemoveManagerFromAllAppsTask;
import com.mryqr.core.assignment.domain.task.RemoveOperatorFromAllAssignmentsTask;
import com.mryqr.core.assignmentplan.domain.task.RemoveOperatorFromAllAssignmentPlansTask;
import com.mryqr.core.department.domain.task.RemoveManagerFromAllDepartmentsTask;
import com.mryqr.core.group.domain.task.RemoveMemberFromAllGroupsTask;
import com.mryqr.core.member.domain.event.MemberDeletedEvent;
import com.mryqr.core.tenant.domain.task.CountMembersForTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDeletedEventHandler extends AbstractDomainEventHandler<MemberDeletedEvent> {
    private final RemoveMemberFromAllGroupsTask removeMemberFromAllGroupsTask;
    private final RemoveManagerFromAllAppsTask removeManagerFromAllAppsTask;
    private final RemoveOperatorFromAllAssignmentsTask removeOperatorFromAllAssignmentsTask;
    private final RemoveManagerFromAllDepartmentsTask removeManagerFromAllDepartmentsTask;
    private final CountMembersForTenantTask countMembersForTenantTask;
    private final RemoveOperatorFromAllAssignmentPlansTask removeOperatorFromAllAssignmentPlansTask;

    @Override
    protected void doHandle(MemberDeletedEvent event) {
        String memberId = event.getMemberId();
        MryTaskRunner.run(() -> removeMemberFromAllGroupsTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> removeManagerFromAllAppsTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> removeOperatorFromAllAssignmentsTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> removeManagerFromAllDepartmentsTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> removeOperatorFromAllAssignmentPlansTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> countMembersForTenantTask.run(event.getArTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
