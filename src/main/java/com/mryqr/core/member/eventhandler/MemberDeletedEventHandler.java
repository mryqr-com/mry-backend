package com.mryqr.core.member.eventhandler;

import com.mryqr.core.app.domain.task.RemoveManagerFromAllAppsTask;
import com.mryqr.core.assignment.domain.task.RemoveOperatorFromAllAssignmentsTask;
import com.mryqr.core.assignmentplan.domain.task.RemoveOperatorFromAllAssignmentPlansTask;
import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.department.domain.task.RemoveManagerFromAllDepartmentsTask;
import com.mryqr.core.group.domain.task.RemoveMemberFromAllGroupsTask;
import com.mryqr.core.member.domain.event.MemberDeletedEvent;
import com.mryqr.core.tenant.domain.task.CountMembersForTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.MEMBER_DELETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberDeletedEventHandler implements DomainEventHandler {
    private final RemoveMemberFromAllGroupsTask removeMemberFromAllGroupsTask;
    private final RemoveManagerFromAllAppsTask removeManagerFromAllAppsTask;
    private final RemoveOperatorFromAllAssignmentsTask removeOperatorFromAllAssignmentsTask;
    private final RemoveManagerFromAllDepartmentsTask removeManagerFromAllDepartmentsTask;
    private final CountMembersForTenantTask countMembersForTenantTask;
    private final RemoveOperatorFromAllAssignmentPlansTask removeOperatorFromAllAssignmentPlansTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == MEMBER_DELETED;
    }

    @Override
    public void handle(DomainEvent domainEvent) {
        MemberDeletedEvent event = (MemberDeletedEvent) domainEvent;
        String memberId = event.getMemberId();
        MryTaskRunner.run(() -> removeMemberFromAllGroupsTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> removeManagerFromAllAppsTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> removeOperatorFromAllAssignmentsTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> removeManagerFromAllDepartmentsTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> removeOperatorFromAllAssignmentPlansTask.run(memberId, event.getArTenantId()));
        MryTaskRunner.run(() -> countMembersForTenantTask.run(event.getArTenantId()));
    }

}
