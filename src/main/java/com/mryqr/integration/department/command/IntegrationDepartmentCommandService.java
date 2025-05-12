package com.mryqr.integration.department.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
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
public class IntegrationDepartmentCommandService {
    private final TenantRepository tenantRepository;
    private final DepartmentFactory departmentFactory;
    private final DepartmentRepository departmentRepository;
    private final MemberRepository memberRepository;
    private final DepartmentDomainService departmentDomainService;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public String createDepartment(IntegrationCreateDepartmentCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:Create", 10);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateAddDepartment();

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(user.getTenantId());
        Department department = departmentFactory.create(command.getName(),
                user.getTenantId(),
                command.getParentDepartmentId(),
                departmentHierarchy, command.getCustomId(),
                user);
        departmentHierarchy.addDepartment(command.getParentDepartmentId(), department.getId(), user);

        departmentRepository.save(department);
        departmentHierarchyRepository.save(departmentHierarchy);
        log.info("Integration created department[{}].", department.getId());
        return department.getId();
    }

    @Transactional
    public void updateDepartmentCustomId(String departmentId, IntegrationUpdateDepartmentCustomIdCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:UpdateCustomId", 10);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        departmentDomainService.updateDepartmentCustomId(department, command.getCustomId(), user);
        departmentRepository.save(department);
        log.info("Integration updated custom ID[{}] for department[{}].", command.getCustomId(), departmentId);
    }

    @Transactional
    public void addDepartmentMember(String departmentId, String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:AddMember", 10);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.addToDepartment(department.getId(), user);
        memberRepository.save(member);
        log.info("Integration added member[{}] to department[{}].", memberId, departmentId);
    }

    @Transactional
    public void addDepartmentMemberByCustomId(String departmentCustomId, String memberCustomId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:Custom:AddMember", 10);

        Department department = departmentRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), departmentCustomId, user);
        Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), memberCustomId, user);
        member.addToDepartment(department.getId(), user);
        memberRepository.save(member);
        log.info("Integration custom added member[{}] to department[{}].", memberCustomId, departmentCustomId);
    }

    @Transactional
    public void removeDepartmentMember(String departmentId, String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:RemoveMember", 10);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        member.removeFromDepartment(department.getId(), user);
        memberRepository.save(member);
        log.info("Integration removed member[{}] from department[{}].", memberId, departmentId);
    }

    @Transactional
    public void removeDepartmentMemberByCustomId(String departmentCustomId, String memberCustomId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:Custom:RemoveMember", 10);

        Department department = departmentRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), departmentCustomId, user);
        Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), memberCustomId, user);
        member.removeFromDepartment(department.getId(), user);
        memberRepository.save(member);
        log.info("Integration custom removed member[{}] from department[{}].", memberCustomId, departmentCustomId);
    }

    @Transactional
    public void addDepartmentManager(String departmentId, String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:AddManager", 10);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        department.addManager(member, user);
        departmentRepository.save(department);
        log.info("Integration added manager[{}] to department[{}].", memberId, departmentId);
    }

    @Transactional
    public void addDepartmentManagerByCustomId(String departmentCustomId, String memberCustomId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:Custom:AddManager", 10);

        Department department = departmentRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), departmentCustomId, user);
        Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), memberCustomId, user);
        department.addManager(member, user);
        departmentRepository.save(department);
        log.info("Integration custom added manager[{}] to department[{}].", memberCustomId, departmentCustomId);
    }

    @Transactional
    public void removeDepartmentManager(String departmentId, String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:RemoveManager", 10);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        Member member = memberRepository.byIdAndCheckTenantShip(memberId, user);
        department.removeManager(member.getId(), user);
        departmentRepository.save(department);
        log.info("Integration removed manager[{}] from department[{}].", memberId, departmentId);
    }

    @Transactional
    public void removeDepartmentManagerByCustomId(String departmentCustomId, String memberCustomId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:Custom:RemoveManager", 10);

        Department department = departmentRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), departmentCustomId, user);
        Member member = memberRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), memberCustomId, user);
        department.removeManager(member.getId(), user);
        departmentRepository.save(department);
        log.info("Integration custom removed manager[{}] from department[{}].", memberCustomId, departmentCustomId);
    }

    @Transactional
    public void deleteDepartment(String departmentId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:Delete", 10);

        Department department = departmentRepository.byIdAndCheckTenantShip(departmentId, user);
        doDeleteDepartment(department, user);
        log.info("Integration deleted department[{}].", departmentId);
    }

    @Transactional
    public void deleteDepartmentByCustomId(String departmentCustomId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Department:Custom:Delete", 10);
        Department department = departmentRepository.byCustomIdAndCheckTenantShip(user.getTenantId(), departmentCustomId, user);
        doDeleteDepartment(department, user);
        log.info("Integration custom deleted department[{}].", departmentCustomId);
    }

    private void doDeleteDepartment(Department department, User user) {
        department.onDelete(user);
        departmentRepository.delete(department);

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(department.getTenantId());
        Set<String> subDepartmentIds = departmentHierarchy.allSubDepartmentIdsOf(department.getId());

        if (isNotEmpty(subDepartmentIds)) {
            List<Department> subDepartments = departmentRepository.byIds(subDepartmentIds);
            subDepartments.forEach(it -> it.onDelete(user));
            departmentRepository.delete(subDepartments);
        }

        departmentHierarchy.removeDepartment(department.getId(), user);
        departmentHierarchyRepository.save(departmentHierarchy);
    }
}
