package com.mryqr.core.group.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.event.AppAwareDomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.GROUP_DEACTIVATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("GROUP_DEACTIVATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class GroupDeactivatedEvent extends AppAwareDomainEvent {
    private String groupId;

    public GroupDeactivatedEvent(String groupId, String appId, User user) {
        super(GROUP_DEACTIVATED, appId, user);
        this.groupId = groupId;
    }
}
