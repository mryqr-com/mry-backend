package com.mryqr.core.tenant.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import com.mryqr.common.event.DomainEventType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public abstract class TenantUpdatedEvent extends DomainEvent {
    private String tenantId;

    public TenantUpdatedEvent(DomainEventType type, String tenantId, User user) {
        super(type, user);
        this.tenantId = tenantId;
    }
}
