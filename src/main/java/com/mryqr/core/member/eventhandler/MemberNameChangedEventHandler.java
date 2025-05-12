package com.mryqr.core.member.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.member.domain.event.MemberNameChangedEvent;
import com.mryqr.core.member.domain.task.SyncMemberNameToAggregateRootsTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.MEMBER_NAME_CHANGED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberNameChangedEventHandler implements DomainEventHandler {
    private final SyncMemberNameToAggregateRootsTask syncMemberNameToAggregateRootsTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == MEMBER_NAME_CHANGED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        MemberNameChangedEvent event = (MemberNameChangedEvent) domainEvent;
        taskRunner.run(() -> syncMemberNameToAggregateRootsTask.run(event.getMemberId()));
    }

}
