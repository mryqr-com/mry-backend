package com.mryqr.core.group.domain.event;

import com.mryqr.core.app.domain.event.AppAwareDomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.GROUP_MANAGERS_CHANGED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("GROUP_MANAGERS_CHANGED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class GroupManagersChangedEvent extends AppAwareDomainEvent {
    private String groupId;

    public GroupManagersChangedEvent(String groupId, String appId, User user) {
        super(GROUP_MANAGERS_CHANGED, appId, user);
        this.groupId = groupId;
    }
}
