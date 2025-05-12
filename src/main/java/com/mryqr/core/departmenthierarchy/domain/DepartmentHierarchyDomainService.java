package com.mryqr.core.departmenthierarchy.domain;

import com.mryqr.core.common.domain.idnode.IdTree;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.department.domain.TenantCachedDepartment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.DEPARTMENT_HIERARCHY_NOT_MATCH;
import static com.mryqr.core.common.exception.ErrorCode.DEPARTMENT_NAME_DUPLICATES;
import static java.util.Set.copyOf;

@Component
@RequiredArgsConstructor
public class DepartmentHierarchyDomainService {
    private final DepartmentRepository departmentRepository;

    public void updateDepartmentHierarchy(DepartmentHierarchy departmentHierarchy, IdTree idTree, User user) {
        departmentHierarchy.update(idTree, user);

        Set<String> providedAllDepartmentIds = departmentHierarchy.allDepartmentIds();
        List<TenantCachedDepartment> cachedDepartments = departmentRepository.cachedTenantAllDepartments(departmentHierarchy.getTenantId());

        Set<String> allDepartmentIds = cachedDepartments.stream()
                .map(TenantCachedDepartment::getId)
                .collect(toImmutableSet());
        if (!Objects.equals(allDepartmentIds, providedAllDepartmentIds)) {
            throw new MryException(DEPARTMENT_HIERARCHY_NOT_MATCH, "部门层级与已有部门不匹配。");
        }

        Map<String, String> allDepartmentNames = cachedDepartments.stream()
                .collect(toImmutableMap(TenantCachedDepartment::getId, TenantCachedDepartment::getName));
        Map<String, String> allFullNames = departmentHierarchy.departmentFullNames(allDepartmentNames);
        if (allFullNames.size() > copyOf(allFullNames.values()).size()) {
            throw new MryException(DEPARTMENT_NAME_DUPLICATES, "更新失败，存在名称重复。", "tenantId", departmentHierarchy.getTenantId());
        }
    }

}
