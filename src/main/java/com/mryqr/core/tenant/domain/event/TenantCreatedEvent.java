package com.mryqr.core.tenant.domain.event;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_CREATED;
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
