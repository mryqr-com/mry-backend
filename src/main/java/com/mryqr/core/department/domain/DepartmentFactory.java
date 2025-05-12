package com.mryqr.core.department.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.DEPARTMENT_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.common.utils.MapUtils.mapOf;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentFactory {
    private final DepartmentRepository departmentRepository;

    public Department create(String name,
                             String tenantId,
                             String parentDepartmentId,
                             DepartmentHierarchy departmentHierarchy,
                             User user) {
        return this.create(name, tenantId, parentDepartmentId, departmentHierarchy, null, user);
    }

    public Department create(String name,
                             String tenantId,
                             String parentDepartmentId,
                             DepartmentHierarchy departmentHierarchy,
                             String customId,
                             User user) {
        checkNameDuplication(name, tenantId, parentDepartmentId, departmentHierarchy);
        return new Department(name, customId, user);
    }

    private void checkNameDuplication(String name,
                                      String tenantId,
                                      String parentDepartmentId,
                                      DepartmentHierarchy departmentHierarchy) {
        Set<String> siblingDepartmentIds = departmentHierarchy.directChildDepartmentIdsUnder(parentDepartmentId);
        Set<String> siblingDepartmentNames = departmentRepository.cachedTenantAllDepartments(tenantId).stream()
                .filter(cachedDepartment -> siblingDepartmentIds.contains(cachedDepartment.getId()))
                .map(TenantCachedDepartment::getName)
                .collect(toImmutableSet());

        if (siblingDepartmentNames.contains(name)) {
            throw new MryException(DEPARTMENT_WITH_NAME_ALREADY_EXISTS, "创建失败，名称已被占用。",
                    mapOf("name", name, "tenantId", tenantId));
        }
    }

}
