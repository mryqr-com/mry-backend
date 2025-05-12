package com.mryqr.core.departmenthierarchy.infrastructure;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.mryqr.common.exception.ErrorCode.DEPARTMENT_HIERARCHY_NOT_FOUND;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Repository
@RequiredArgsConstructor
public class MongoDepartmentHierarchyRepository extends MongoBaseRepository<DepartmentHierarchy> implements DepartmentHierarchyRepository {
    private final MongoTemplate mongoTemplate;
    private final MongoCachedDepartmentHierarchyRepository cachedDepartmentHierarchyRepository;

    @Override
    public DepartmentHierarchy byTenantId(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("tenantId").is(tenantId));
        DepartmentHierarchy departmentHierarchy = mongoTemplate.findOne(query, DepartmentHierarchy.class);

        if (departmentHierarchy == null) {
            throw new MryException(DEPARTMENT_HIERARCHY_NOT_FOUND, "未找到部门层级。", mapOf("tenantId", tenantId));
        }

        return departmentHierarchy;
    }

    @Override
    public DepartmentHierarchy cachedByTenantId(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return cachedDepartmentHierarchyRepository.cachedByTenantId(tenantId);
    }

    @Override
    public void save(DepartmentHierarchy departmentHierarchy) {
        super.save(departmentHierarchy);
        cachedDepartmentHierarchyRepository.evictDepartmentHierarchyCache(departmentHierarchy.getTenantId());
    }

    @Override
    public void evictDepartmentHierarchyCache(String tenantId) {
        cachedDepartmentHierarchyRepository.evictDepartmentHierarchyCache(tenantId);
    }
}
