package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.APP_GROUP_SYNC_ENABLED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("APP_GROUP_SYNC_ENABLED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class AppGroupSyncEnabledEvent extends AppAwareDomainEvent {

    public AppGroupSyncEnabledEvent(String appId, User user) {
        super(APP_GROUP_SYNC_ENABLED, appId, user);
    }
}
