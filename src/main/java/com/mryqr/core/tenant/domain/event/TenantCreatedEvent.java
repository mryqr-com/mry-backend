package com.mryqr.core.tenant.domain.event;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.event.DomainEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.TENANT_CREATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_CREATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantCreatedEvent extends DomainEvent {
    private String tenantId;

    public TenantCreatedEvent(String tenantId, User user) {
        super(TENANT_CREATED, user);
        this.tenantId = tenantId;
    }
}
