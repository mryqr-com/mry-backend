package com.mryqr.core.department.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.department.domain.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveManagerFromAllDepartmentsTask implements RepeatableTask {
    private final DepartmentRepository departmentRepository;

    public void run(String memberId, String tenantId) {
        int count = departmentRepository.removeManagerFromAllDepartments(memberId, tenantId);
        log.info("Removed manager[{}] from all {} departments of tenant[{}].", memberId, count, tenantId);

        if (count > 0) {
            departmentRepository.evictTenantDepartmentsCache(tenantId);
        }
    }
}
