package com.mryqr.core.group.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.event.AppAwareDomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.GROUP_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("GROUP_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class GroupCreatedEvent extends AppAwareDomainEvent {
    private String groupId;

    public GroupCreatedEvent(String groupId, String appId, User user) {
        super(GROUP_CREATED, appId, user);
        this.groupId = groupId;
    }
}
