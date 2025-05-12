package com.mryqr.core.member.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.member.domain.event.MemberCreatedEvent;
import com.mryqr.core.tenant.domain.task.CountMembersForTenantTask;
import com.mryqr.core.tenant.domain.task.DeltaCountMemberForTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.MEMBER_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCreatedEventHandler implements DomainEventHandler {
    private final CountMembersForTenantTask countMembersForTenantTask;
    private final DeltaCountMemberForTenantTask deltaCountMemberForTenantTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == MEMBER_CREATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        MemberCreatedEvent event = (MemberCreatedEvent) domainEvent;

        if (event.isNotConsumedBefore()) {
            taskRunner.run(() -> deltaCountMemberForTenantTask.delta(event.getArTenantId(), 1));
        } else {
            taskRunner.run(() -> countMembersForTenantTask.run(event.getArTenantId()));
        }
    }

}
