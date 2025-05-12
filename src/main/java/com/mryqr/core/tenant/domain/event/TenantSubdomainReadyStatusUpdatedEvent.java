package com.mryqr.core.tenant.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.TENANT_SUBDOMAIN_READY_STATUS_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_SUBDOMAIN_READY_STATUS_UPDATE_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantSubdomainReadyStatusUpdatedEvent extends TenantUpdatedEvent {
    private boolean ready;

    public TenantSubdomainReadyStatusUpdatedEvent(String tenantId, boolean ready, User user) {
        super(TENANT_SUBDOMAIN_READY_STATUS_UPDATED, tenantId, user);
        this.ready = ready;
    }
}
