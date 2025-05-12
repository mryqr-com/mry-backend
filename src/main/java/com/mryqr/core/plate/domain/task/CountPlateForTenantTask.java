package com.mryqr.core.plate.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountPlateForTenantTask implements RepeatableTask {
    private final TenantRepository tenantRepository;
    private final PlateRepository plateRepository;

    public void run(String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = plateRepository.countPlateUnderTenant(tenantId);
            tenant.setPlateCount(count);
            tenantRepository.save(tenant);
            log.info("Counted all {} plates for tenant[{}].", count, tenantId);
        });
    }

}
