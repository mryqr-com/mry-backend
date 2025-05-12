package com.mryqr.core.department.infrastructure;

import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.department.domain.TenantCachedDepartment;
import com.mryqr.core.department.domain.TenantCachedDepartments;
import com.mryqr.core.member.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.common.utils.MryConstants.DEPARTMENT_COLLECTION;
import static com.mryqr.common.utils.MryConstants.TENANT_DEPARTMENTS_CACHE;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

//为了绕开Spring AOP必须从外部调用才生效的限制，否则方法可以直接放到DepartmentRepository中
//不要直接使用，而是使用DepartmentRepository中同名方法
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoCachedDepartmentRepository extends MongoBaseRepository<Member> {

    @Cacheable(value = TENANT_DEPARTMENTS_CACHE, key = "#tenantId")
    public TenantCachedDepartments cachedTenantAllDepartments(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId));
        query.fields().include("name", "managers", "customId");

        List<TenantCachedDepartment> tenantCachedDepartments = mongoTemplate.find(query, TenantCachedDepartment.class,
                DEPARTMENT_COLLECTION);
        return TenantCachedDepartments.builder().departments(emptyIfNull(tenantCachedDepartments)).build();
    }

    @Caching(evict = {@CacheEvict(value = TENANT_DEPARTMENTS_CACHE, key = "#tenantId")})
    public void evictTenantDepartmentsCache(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        log.debug("Evicted all departments cache for tenant[{}].", tenantId);
    }
}
