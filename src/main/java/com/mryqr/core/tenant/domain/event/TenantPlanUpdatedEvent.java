package com.mryqr.core.tenant.domain.event;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.plan.domain.PlanType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_PLAN_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_PLAN_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantPlanUpdatedEvent extends TenantUpdatedEvent {
    private PlanType planType;

    public TenantPlanUpdatedEvent(String tenantId, PlanType planType, User user) {
        super(TENANT_PLAN_UPDATED, tenantId, user);
        this.planType = planType;
    }
}
