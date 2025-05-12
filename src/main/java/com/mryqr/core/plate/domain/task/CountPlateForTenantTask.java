package com.mryqr.core.plate.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountPlateForTenantTask implements RetryableTask {
    private final TenantRepository tenantRepository;
    private final PlateRepository plateRepository;

    public void run(String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = plateRepository.countPlateUnderTenant(tenantId);
            tenant.setPlateCount(count);
            tenantRepository.save(tenant);
            log.debug("Counted all {} plates for tenant[{}].", count, tenantId);
        });
    }

}
