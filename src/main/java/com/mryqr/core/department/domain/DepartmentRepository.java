package com.mryqr.core.department.domain;

import com.mryqr.common.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DepartmentRepository {
    Department byCustomIdAndCheckTenantShip(String tenantId, String customId, User user);

    List<TenantCachedDepartment> cachedTenantAllDepartments(String tenantId);

    boolean cachedNotAllDepartmentsExist(List<String> departmentIds, String tenantId);

    boolean cachedExistsByCustomId(String customId, String tenantId);

    Department byId(String id);

    Optional<Department> byIdOptional(String id);

    Department byIdAndCheckTenantShip(String id, User user);

    List<Department> byIds(Set<String> ids);

    boolean exists(String arId);

    void save(Department department);

    void delete(Department department);

    void delete(List<Department> departments);

    void evictTenantDepartmentsCache(String tenantId);

    int countDepartmentForTenant(String tenantId);

    int removeManagerFromAllDepartments(String memberId, String tenantId);
}
