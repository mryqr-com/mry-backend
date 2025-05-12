package com.mryqr.core.tenant.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.TENANT_RESOURCE_USAGE_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_RESOURCE_USAGE_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantResourceUsageUpdatedEvent extends TenantUpdatedEvent {

    public TenantResourceUsageUpdatedEvent(String tenantId, User user) {
        super(TENANT_RESOURCE_USAGE_UPDATED, tenantId, user);
    }
}
