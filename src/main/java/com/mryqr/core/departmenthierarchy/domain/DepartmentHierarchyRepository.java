package com.mryqr.core.departmenthierarchy.domain;

public interface DepartmentHierarchyRepository {
    DepartmentHierarchy byTenantId(String tenantId);

    DepartmentHierarchy cachedByTenantId(String tenantId);

    void save(DepartmentHierarchy departmentHierarchy);

    void evictDepartmentHierarchyCache(String tenantId);
}
