package com.mryqr.core.app.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.event.DomainEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class AppAwareDomainEvent extends DomainEvent {
    private String appId;

    public AppAwareDomainEvent(DomainEventType type, String appId, User user) {
        super(type, user);
        this.appId = appId;
    }
}
