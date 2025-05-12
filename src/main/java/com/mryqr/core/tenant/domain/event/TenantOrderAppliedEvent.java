package com.mryqr.core.tenant.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_ORDER_APPLIED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_ORDER_APPLIED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantOrderAppliedEvent extends TenantUpdatedEvent {
    private String orderId;

    public TenantOrderAppliedEvent(String tenantId, String orderId, User user) {
        super(TENANT_ORDER_APPLIED, tenantId, user);
        this.orderId = orderId;
    }
}
