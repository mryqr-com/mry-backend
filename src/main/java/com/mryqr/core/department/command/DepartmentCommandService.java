package com.mryqr.core.department.command;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.department.domain.Department;
import com.mryqr.core.department.domain.DepartmentDomainService;
import com.mryqr.core.department.domain.DepartmentFactory;
import com.mryqr.core.department.domain.DepartmentRepository;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepartmentCommandService {
    private final TenantRepository tenantRepository;
    private final DepartmentFactory departmentFactory;
    private final DepartmentRepository departmentRepository;
    private final DepartmentDomainService departmentDomainService;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final MryRateLimiter mryRateLimiter;
    private final MemberRepository memberRepository;

    @Transactional
    public String createDepartment(CreateDepartmentCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Department:Create", 1);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateAddDepartment();

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(user.getTenantId());
        Department department = departmentFactory.create(command.getName(),
                user.getTenantId(),
                command.getParentDepartmentId(),
                departmentHierarchy,
                user);
        departmentHierarchy.addDepartment(command.getParentDepartmentId(), department.getId(), user);

        departmentRepository.save(department);
        departmentHierarchyRepository.save(departmentHierarchy);
        log.info("Created department[{}].", department.getId());
        return department.getId();
    }

    @Transactional
    public void renameDepartment(String departmentId, RenameDepartmentCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Department:Rename", 1);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        departmentDomainService.renameDepartment(department, command.getName(), user);
        departmentRepository.save(department);
        log.info("Renamed department[{}].", departmentId);
    }

    @Transactional
    public void addDepartmentManager(String departmentId, String memberId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Department:AddManager", 1);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        department.addManager(member, user);
        departmentRepository.save(department);
        log.info("Added manager[{}] to department[{}].", memberId, departmentId);
    }

    @Transactional
    public void removeDepartmentManager(String departmentId, String memberId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Department:RemoveManager", 1);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        department.removeManager(memberId, user);
        departmentRepository.save(department);
        log.info("Removed manager[{}] from department[{}].", memberId, departmentId);
    }

    @Transactional
    public void deleteDepartment(String departmentId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Department:Delete", 1);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        department.onDelete(user);
        departmentRepository.delete(department);

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(department.getTenantId());
        Set<String> subDepartmentIds = departmentHierarchy.allSubDepartmentIdsOf(departmentId);

        if (isNotEmpty(subDepartmentIds)) {
            List<Department> subDepartments = departmentRepository.byIds(subDepartmentIds);
            subDepartments.forEach(it -> it.onDelete(user));
            departmentRepository.delete(subDepartments);
        }

        departmentHierarchy.removeDepartment(departmentId, user);
        departmentHierarchyRepository.save(departmentHierarchy);
        log.info("Deleted department[{}].", departmentId);
    }
}
