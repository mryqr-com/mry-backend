package com.mryqr.core.department.domain;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.DEPARTMENT_WITH_CUSTOM_ID_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.DEPARTMENT_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class DepartmentDomainService {
    private final DepartmentRepository departmentRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;

    public void renameDepartment(Department department, String newName, User user) {
        DepartmentHierarchy groupHierarchy = departmentHierarchyRepository.byTenantId(department.getTenantId());

        Set<String> siblingGroupIds = groupHierarchy.siblingDepartmentIdsOf(department.getId());
        if (isNotEmpty(siblingGroupIds)) {
            Set<String> siblingGroupNames = departmentRepository.cachedTenantAllDepartments(department.getTenantId()).stream()
                    .filter(cachedGroup -> siblingGroupIds.contains(cachedGroup.getId()))
                    .map(TenantCachedDepartment::getName)
                    .collect(toImmutableSet());

            if (siblingGroupNames.contains(newName)) {
                throw new MryException(DEPARTMENT_WITH_NAME_ALREADY_EXISTS, "重命名失败，名称已被占用。",
                        mapOf("departmentId", department.getId(), "name", newName));
            }
        }

        department.rename(newName, user);
    }

    public void updateDepartmentCustomId(Department department, String customId, User user) {
        if (isNotBlank(customId) &&
                !Objects.equals(department.getCustomId(), customId) &&
                departmentRepository.cachedExistsByCustomId(customId, department.getTenantId())) {
            throw new MryException(DEPARTMENT_WITH_CUSTOM_ID_ALREADY_EXISTS,
                    "自定义编号已被占用。",
                    mapOf("departmentId", department.getId(), "tenantId", department.getTenantId()));
        }

        department.updateCustomId(customId, user);
    }

}
