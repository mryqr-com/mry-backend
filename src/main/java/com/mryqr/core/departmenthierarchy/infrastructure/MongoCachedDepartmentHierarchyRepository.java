package com.mryqr.core.departmenthierarchy.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import static com.mryqr.core.common.exception.ErrorCode.DEPARTMENT_HIERARCHY_NOT_FOUND;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.DEPARTMENT_HIERARCHY_CACHE;
import static org.springframework.data.mongodb.core.query.Criteria.where;

//为了绕开Spring AOP必须从外部调用才生效的限制，否则方法可以直接放到DepartmentHierarchyRepository中
//不要直接使用，而是使用DepartmentHierarchyRepository中同名方法

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedDepartmentHierarchyRepository extends MongoBaseRepository<DepartmentHierarchy> {

    @Cacheable(value = DEPARTMENT_HIERARCHY_CACHE, key = "#tenantId")
    public DepartmentHierarchy cachedByTenantId(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = Query.query(where("tenantId").is(tenantId));
        DepartmentHierarchy departmentHierarchy = mongoTemplate.findOne(query, DepartmentHierarchy.class);

        if (departmentHierarchy == null) {
            throw new MryException(DEPARTMENT_HIERARCHY_NOT_FOUND, "未找到部门层级。", mapOf("tenantId", tenantId));
        }

        return departmentHierarchy;
    }

    @Caching(evict = {@CacheEvict(value = DEPARTMENT_HIERARCHY_CACHE, key = "#tenantId")})
    public void evictDepartmentHierarchyCache(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        log.info("Evicted department hierarchy cache for tenant[{}].", tenantId);
    }

}
