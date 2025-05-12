package com.mryqr.core.app.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.GROUP_SYNC_ENABLED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("GROUP_SYNC_ENABLED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class GroupSyncEnabledEvent extends AppAwareDomainEvent {

    public GroupSyncEnabledEvent(String appId, User user) {
        super(GROUP_SYNC_ENABLED, appId, user);
    }
}
