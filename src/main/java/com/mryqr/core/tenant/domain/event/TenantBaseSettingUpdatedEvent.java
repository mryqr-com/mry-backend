package com.mryqr.core.tenant.domain.event;

import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

import static com.mryqr.core.common.domain.event.DomainEventType.TENANT_BASE_SETTING_UPDATED;
import static lombok.AccessLevel.PRIVATE;

@Getter
@TypeAlias("TENANT_BASE_SETTING_UPDATED_EVENT")
@NoArgsConstructor(access = PRIVATE)
public class TenantBaseSettingUpdatedEvent extends TenantUpdatedEvent {

    public TenantBaseSettingUpdatedEvent(String tenantId, User user) {
        super(TENANT_BASE_SETTING_UPDATED, tenantId, user);
    }
}
