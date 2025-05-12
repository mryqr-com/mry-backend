package com.mryqr.core.tenant.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAppUsageFromTenantTask implements RepeatableTask {
    private final TenantRepository tenantRepository;

    public void run(String tenantId, String appId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            tenant.removeAppUsage(appId);
            tenantRepository.save(tenant);
            log.info("Removed app[{}] usage data of tenant[{}].", appId, tenantId);
        });
    }
}
