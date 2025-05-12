package com.mryqr.core.tenant.domain.task;

import com.mryqr.core.common.domain.task.OnetimeTask;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantSmsUsageCountTask implements OnetimeTask {
    private final TenantRepository tenantRepository;

    @Transactional
    public void run(String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            tenant.useSms();
            tenantRepository.save(tenant);
        });
    }
}
