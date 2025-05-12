package com.mryqr.core.department.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountDepartmentForTenantTask implements RepeatableTask {
    private final TenantRepository tenantRepository;
    private final DepartmentRepository departmentRepository;

    public void run(String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = departmentRepository.countDepartmentForTenant(tenantId);
            tenant.setDepartmentCount(count);
            tenantRepository.save(tenant);
            log.info("Counted all {} departments for tenant[{}].", count, tenantId);
        });
    }
}
