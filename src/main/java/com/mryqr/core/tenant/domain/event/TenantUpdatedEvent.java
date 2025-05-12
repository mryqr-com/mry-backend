package com.mryqr.core.tenant.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventType;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
public class TenantUpdatedEvent extends DomainEvent {
    private String tenantId;

    public TenantUpdatedEvent(DomainEventType type, String tenantId, User user) {
        super(type, user);
        this.tenantId = tenantId;
    }
}
