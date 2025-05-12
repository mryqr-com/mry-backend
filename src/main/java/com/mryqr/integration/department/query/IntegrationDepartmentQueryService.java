package com.mryqr.integration.department.query;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.department.domain.Department;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.mryqr.core.common.utils.MryConstants.DEPARTMENT_COLLECTION;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class IntegrationDepartmentQueryService {
    private final MryRateLimiter mryRateLimiter;
    private final MongoTemplate mongoTemplate;
    private final DepartmentRepository departmentRepository;
    private final MemberRepository memberRepository;

    public List<QIntegrationListDepartment> listDepartments(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:List", 10);

        Query query = Query.query(where("tenantId").is(user.getTenantId()));
        query.fields().include("name", "customId");

        return mongoTemplate.find(query, QIntegrationListDepartment.class, DEPARTMENT_COLLECTION);
    }

    public QIntegrationDepartment fetchDepartment(String departmentId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:Fetch", 10);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);

        Set<String> members = memberRepository.cachedMemberIdsOfDepartment(user.getTenantId(), department.getId());
        return QIntegrationDepartment.builder()
                .id(department.getId())
                .name(department.getName())
                .customId(department.getCustomId())
                .managers(department.getManagers())
                .members(members)
                .createdAt(department.getCreatedAt())
                .build();
    }

    public QIntegrationDepartment fetchDepartmentByCustomId(String departmentCustomId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:Custom:LFetch", 10);

        Department department = departmentRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), departmentCustomId, user);

        Set<String> members = memberRepository.cachedMemberIdsOfDepartment(user.getTenantId(), department.getId());
        return QIntegrationDepartment.builder()
                .id(department.getId())
                .name(department.getName())
                .customId(department.getCustomId())
                .managers(department.getManagers())
                .members(members)
                .createdAt(department.getCreatedAt())
                .build();
    }

}
