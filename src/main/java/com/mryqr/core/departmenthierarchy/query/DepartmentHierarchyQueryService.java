package com.mryqr.core.departmenthierarchy.query;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import com.mryqr.core.departmenthierarchy.query.QDepartmentHierarchy.QHierarchyDepartment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
@RequiredArgsConstructor
public class DepartmentHierarchyQueryService {
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final DepartmentRepository departmentRepository;
    private final MryRateLimiter mryRateLimiter;

    public QDepartmentHierarchy fetchDepartmentHierarchy(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "DepartmentHierarchy:Fetch", 1);

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(user.getTenantId());
        List<QHierarchyDepartment> allDepartments = departmentRepository.cachedTenantAllDepartments(user.getTenantId()).stream()
                .map(group -> QHierarchyDepartment.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .managers(group.getManagers())
                        .build())
                .collect(toImmutableList());

        return QDepartmentHierarchy.builder()
                .idTree(departmentHierarchy.getIdTree())
                .allDepartments(allDepartments)
                .build();
    }
}
