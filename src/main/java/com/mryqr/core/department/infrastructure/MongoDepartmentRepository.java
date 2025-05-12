package com.mryqr.core.department.infrastructure;

import com.mongodb.client.result.UpdateResult;
import com.mryqr.common.mongo.MongoBaseRepository;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.department.domain.Department;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.department.domain.TenantCachedDepartment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.mryqr.core.common.exception.ErrorCode.DEPARTMENT_NOT_FOUND;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class MongoDepartmentRepository extends MongoBaseRepository<Department> implements DepartmentRepository {
    private final MongoCachedDepartmentRepository cachedDepartmentRepository;

    @Override
    public Department byCustomIdAndCheckTenantShip(String tenantId, String customId, User user) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");
        requireNonBlank(customId, "Custom ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId).and("customId").is(customId));
        Department department = mongoTemplate.findOne(query, Department.class);

        if (department == null) {
            throw new MryException(DEPARTMENT_NOT_FOUND, "未找到部门。", mapOf("tenantId", tenantId, "customId", customId));
        }

        checkTenantShip(department, user);
        return department;
    }

    @Override
    public List<TenantCachedDepartment> cachedTenantAllDepartments(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        return cachedDepartmentRepository.cachedTenantAllDepartments(tenantId);
    }

    @Override
    public boolean cachedNotAllDepartmentsExist(List<String> departmentIds, String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        if (isEmpty(departmentIds)) {
            return false;
        }

        return !cachedDepartmentRepository.cachedTenantAllDepartments(tenantId).stream()
                .map(TenantCachedDepartment::getId).toList()
                .containsAll(departmentIds);
    }

    @Override
    public boolean cachedExistsByCustomId(String customId, String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");
        requireNonBlank(customId, "Custom ID must not be blank.");

        return cachedDepartmentRepository.cachedTenantAllDepartments(tenantId).stream()
                .anyMatch(department -> Objects.equals(department.getCustomId(), customId));
    }

    @Override
    public Department byId(String id) {
        return super.byId(id);
    }

    @Override
    public Optional<Department> byIdOptional(String id) {
        return super.byIdOptional(id);
    }

    @Override
    public Department byIdAndCheckTenantShip(String id, User user) {
        return super.byIdAndCheckTenantShip(id, user);
    }

    @Override
    public List<Department> byIds(Set<String> ids) {
        return super.byIds(ids);
    }

    @Override
    public boolean exists(String arId) {
        return super.exists(arId);
    }

    @Override
    public void save(Department department) {
        super.save(department);
        cachedDepartmentRepository.evictTenantDepartmentsCache(department.getTenantId());
    }

    @Override
    public void delete(Department department) {
        super.delete(department);
        cachedDepartmentRepository.evictTenantDepartmentsCache(department.getTenantId());
    }

    @Override
    public void delete(List<Department> departments) {
        if (isEmpty(departments)) {
            return;
        }

        super.delete(departments);
        departments.stream().findAny().ifPresent(department -> cachedDepartmentRepository.evictTenantDepartmentsCache(department.getTenantId()));
    }

    @Override
    public void evictTenantDepartmentsCache(String tenantId) {
        cachedDepartmentRepository.evictTenantDepartmentsCache(tenantId);
    }

    @Override
    public int countDepartmentForTenant(String tenantId) {
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("tenantId").is(tenantId));
        return (int) mongoTemplate.count(query, Department.class);
    }

    @Override
    public int removeManagerFromAllDepartments(String memberId, String tenantId) {
        requireNonBlank(memberId, "Member ID must not be blank.");
        requireNonBlank(tenantId, "Tenant ID must not be blank.");

        Query query = query(where("managers").is(memberId));
        Update update = new Update().pull("managers", memberId);
        UpdateResult result = mongoTemplate.updateMulti(query, update, Department.class);
        return (int) result.getModifiedCount();
    }
}
