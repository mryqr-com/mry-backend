package com.mryqr.core.tenant.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_ACTIVATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_ACTIVATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantActivatedEvent extends TenantUpdatedEvent {

    public TenantActivatedEvent(String tenantId, User user) {
        super(TENANT_ACTIVATED, tenantId, user);
    }
}
