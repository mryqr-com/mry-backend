package com.mryqr.core.member.eventhandler;

import com.mryqr.common.event.consume.DomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.member.domain.event.MemberNameChangedEvent;
import com.mryqr.core.member.domain.task.SyncMemberNameToAggregateRootsTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberNameChangedEventHandler extends DomainEventHandler<MemberNameChangedEvent> {
    private final SyncMemberNameToAggregateRootsTask syncMemberNameToAggregateRootsTask;

    @Override
    public void handle(MemberNameChangedEvent event) {
        MryTaskRunner.run(() -> syncMemberNameToAggregateRootsTask.run(event.getMemberId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
