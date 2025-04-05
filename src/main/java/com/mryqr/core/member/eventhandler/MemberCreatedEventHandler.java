package com.mryqr.core.member.eventhandler;

import com.mryqr.common.event.consume.DomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.member.domain.event.MemberCreatedEvent;
import com.mryqr.core.tenant.domain.task.CountMembersForTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberCreatedEventHandler extends DomainEventHandler<MemberCreatedEvent> {
    private final CountMembersForTenantTask countMembersForTenantTask;

    @Override
    public void handle(MemberCreatedEvent event) {
        MryTaskRunner.run(() -> countMembersForTenantTask.run(event.getArTenantId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
