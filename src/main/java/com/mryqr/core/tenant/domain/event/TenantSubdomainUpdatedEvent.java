package com.mryqr.core.tenant.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.TENANT_SUBDOMAIN_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_SUBDOMAIN_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantSubdomainUpdatedEvent extends TenantUpdatedEvent {
    private String oldSubdomainPrefix;
    private String newSubdomainPrefix;

    public TenantSubdomainUpdatedEvent(String tenantId,
                                       String oldSubdomainPrefix,
                                       String newSubdomainPrefix,
                                       User user) {
        super(TENANT_SUBDOMAIN_UPDATED, tenantId, user);
        this.oldSubdomainPrefix = oldSubdomainPrefix;
        this.newSubdomainPrefix = newSubdomainPrefix;
    }
}
