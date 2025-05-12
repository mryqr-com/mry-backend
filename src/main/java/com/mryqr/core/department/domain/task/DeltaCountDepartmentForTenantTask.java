package com.mryqr.core.department.domain.task;

import com.mryqr.common.domain.task.NonRetryableTask;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeltaCountDepartmentForTenantTask implements NonRetryableTask {
    private final TenantRepository tenantRepository;

    public void delta(String tenantId, int delta) {
        int modifiedCount = tenantRepository.deltaCountDepartment(tenantId, delta);
        if (modifiedCount > 0) {
            log.info("Delta counted departments for tenant[{}] by {}.", tenantId, delta);
        }
    }
}
