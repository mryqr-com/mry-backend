package com.mryqr.core.plate.domain.task;

import com.mryqr.common.domain.task.NonRetryableTask;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeltaCountPlateForTenantTask implements NonRetryableTask {
    private final TenantRepository tenantRepository;

    public void delta(String tenantId, int delta) {
        int modifiedCount = tenantRepository.deltaCountPlate(tenantId, delta);
        if (modifiedCount > 0) {
            log.info("Delta counted plates for tenant[{}] by {}.", tenantId, delta);
        }
    }
}
