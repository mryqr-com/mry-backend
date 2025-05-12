package com.mryqr.core.tenant.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_DEACTIVATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_DEACTIVATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantDeactivatedEvent extends TenantUpdatedEvent {

    public TenantDeactivatedEvent(String tenantId, User user) {
        super(TENANT_DEACTIVATED, tenantId, user);
    }
}
