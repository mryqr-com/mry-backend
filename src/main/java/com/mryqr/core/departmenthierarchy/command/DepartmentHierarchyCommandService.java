package com.mryqr.core.departmenthierarchy.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyDomainService;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentHierarchyCommandService {
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final DepartmentHierarchyDomainService departmentHierarchyDomainService;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public void updateDepartmentHierarchy(UpdateDepartmentHierarchyCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "DepartmentHierarchy:Update", 1);

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(user.getTenantId());
        departmentHierarchyDomainService.updateDepartmentHierarchy(departmentHierarchy, command.getIdTree(), user);

        departmentHierarchyRepository.save(departmentHierarchy);
        log.info("Updated department hierarchy for tenant[{}].", user.getTenantId());
    }
}
