package com.mryqr.core.tenant.domain.event;

import com.mryqr.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.common.event.DomainEventType.TENANT_INVOICE_TITLE_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_INVOICE_TITLE_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantInvoiceTitleUpdatedEvent extends TenantUpdatedEvent {

    public TenantInvoiceTitleUpdatedEvent(String tenantId, User user) {
        super(TENANT_INVOICE_TITLE_UPDATED, tenantId, user);
    }
}
