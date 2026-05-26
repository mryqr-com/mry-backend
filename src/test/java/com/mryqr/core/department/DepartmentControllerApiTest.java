package com.mryqr.core.department;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.department.command.CreateDepartmentCommand;
import com.mryqr.core.department.command.RenameDepartmentCommand;
import com.mryqr.core.department.domain.Department;
import com.mryqr.core.department.domain.TenantCachedDepartment;
import com.mryqr.core.department.domain.event.DepartmentCreatedEvent;
import com.mryqr.core.department.domain.event.DepartmentDeletedEvent;
import com.mryqr.core.department.domain.event.DepartmentManagersChangedEvent;
import com.mryqr.core.department.domain.event.DepartmentRenamedEvent;
import com.mryqr.core.departmenthierarchy.DepartmentHierarchyApi;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.event.DepartmentHierarchyChangedEvent;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.command.CreateMemberCommand;
import com.mryqr.core.member.command.UpdateMemberInfoCommand;
import com.mryqr.core.tenant.domain.ResourceUsage;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.support.PollingAssertion;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static com.mryqr.common.event.DomainEventType.*;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

public class DepartmentControllerApiTest extends BaseApiTest {
    @Test
    public void should_create_department() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentName = rDepartmentName();
        String departmentId = DepartmentApi.createDepartment(response.jwt(), CreateDepartmentCommand.builder().name(departmentName).build());

        Department department = departmentRepository.byId(departmentId);
        assertEquals(departmentName, department.getName());

        Tenant tenant = tenantRepository.byId(department.getTenantId());
        assertEquals(1, tenant.getResourceUsage().getDepartmentCount());

        DepartmentCreatedEvent event = latestEventFor(departmentId, DEPARTMENT_CREATED, DepartmentCreatedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(response.tenantId());
        assertEquals(1, departmentHierarchy.allDepartmentIds().size());
        assertTrue(departmentHierarchy.containsDepartmentId(departmentId));

        assertEquals(response.tenantId(),
                latestEventFor(departmentHierarchy.getId(), DEPARTMENT_HIERARCHY_CHANGED, DepartmentHierarchyChangedEvent.class).getTenantId());
    }

    @Test
    public void should_create_department_with_parent() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String parentDepartmentId = DepartmentApi.createDepartment(loginResponse.jwt(), rDepartmentName());
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(loginResponse.jwt(), parentDepartmentId, rDepartmentName());

        Department department = departmentRepository.byId(subDepartmentId);
        assertNotNull(department);
        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(loginResponse.tenantId());
        departmentHierarchy.containsDepartmentId(parentDepartmentId);
        departmentHierarchy.containsDepartmentId(subDepartmentId);
    }

    @Test
    public void should_create_department_with_same_name_but_different_level() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String parentName = rDepartmentName();
        String childName = rDepartmentName();
        String parentDepartmentId = DepartmentApi.createDepartment(loginResponse.jwt(), parentName);
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(loginResponse.jwt(), parentDepartmentId, childName);
        assertEquals(parentName, departmentRepository.byId(parentDepartmentId).getName());
        assertEquals(childName, departmentRepository.byId(subDepartmentId).getName());
        List<TenantCachedDepartment> tenantCachedDepartments = departmentRepository.cachedTenantAllDepartments(loginResponse.tenantId());
        assertEquals(2, tenantCachedDepartments.size());
    }

    @Test
    public void create_department_should_also_sync_to_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.enableGroupSync(response.jwt(), response.appId());

        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());
        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.jwt(), response.memberId(), command);
        DepartmentApi.addDepartmentManager(response.jwt(), departmentId, response.memberId());
        DepartmentApi.createDepartment(response.jwt(), rDepartmentName());//trigger sync again to sync members
        Optional<Group> group = groupRepository.byDepartmentIdOptional(departmentId, response.appId());
        assertTrue(group.isPresent());
        assertTrue(group.get().getManagers().contains(response.memberId()));
        assertTrue(group.get().getMembers().contains(response.memberId()));
    }

    @Test
    public void should_fail_create_department_if_parent_department_not_exist() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        assertError(() -> DepartmentApi.createDepartmentRaw(loginResponse.jwt(),
                        CreateDepartmentCommand.builder().name(rDepartmentName()).parentDepartmentId(Department.newDepartmentId()).build()),
                DEPARTMENT_NOT_FOUND);
    }

    @Test
    public void should_fail_create_department_if_hierarchy_too_deep() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        String departmentId1 = DepartmentApi.createDepartment(loginResponse.jwt(), rDepartmentName());
        String departmentId2 = DepartmentApi.createDepartmentWithParent(loginResponse.jwt(), departmentId1, rDepartmentName());
        String departmentId3 = DepartmentApi.createDepartmentWithParent(loginResponse.jwt(), departmentId2, rDepartmentName());
        String departmentId4 = DepartmentApi.createDepartmentWithParent(loginResponse.jwt(), departmentId3, rDepartmentName());
        String departmentId5 = DepartmentApi.createDepartmentWithParent(loginResponse.jwt(), departmentId4, rDepartmentName());
        assertError(() -> DepartmentApi.createDepartmentRaw(loginResponse.jwt(),
                        CreateDepartmentCommand.builder().name(rDepartmentName()).parentDepartmentId(departmentId5).build()),
                DEPARTMENT_HIERARCHY_TOO_DEEP);
    }

    @Test
    public void should_fail_create_department_if_max_limit_reached() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        Tenant theTenant = tenantRepository.byId(loginResponse.tenantId());
        theTenant.setDepartmentCount(theTenant.currentPlan().getMaxDepartmentCount());
        tenantRepository.save(theTenant);
        assertError(
                () -> DepartmentApi.createDepartmentRaw(loginResponse.jwt(), CreateDepartmentCommand.builder().name(rDepartmentName()).build()),
                DEPARTMENT_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_fail_create_department_if_name_already_exists() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateDepartmentCommand command = CreateDepartmentCommand.builder().name(rDepartmentName()).build();
        DepartmentApi.createDepartment(response.jwt(), command);

        assertError(() -> DepartmentApi.createDepartmentRaw(response.jwt(), command), DEPARTMENT_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_department_if_exceed_max_count() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.tenantId());
        ResourceUsage resourceUsage = tenant.getResourceUsage();
        ReflectionTestUtils.setField(resourceUsage, "departmentCount", tenant.getPackages().effectiveMaxDepartmentCount());
        tenantRepository.save(tenant);

        CreateDepartmentCommand command = CreateDepartmentCommand.builder().name(rDepartmentName()).build();
        assertError(() -> DepartmentApi.createDepartmentRaw(response.jwt(), command), DEPARTMENT_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_rename_department() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId = DepartmentApi.createDepartment(response.jwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        String newName = rDepartmentName();

        DepartmentApi.renameDepartment(response.jwt(), departmentId, RenameDepartmentCommand.builder().name(newName).build());

        Department department = departmentRepository.byId(departmentId);
        assertEquals(newName, department.getName());

        DepartmentRenamedEvent event = latestEventFor(departmentId, DEPARTMENT_RENAMED, DepartmentRenamedEvent.class);
        assertEquals(department.getId(), event.getDepartmentId());
    }

    @Test
    public void rename_department_should_sync_to_group() {
        PreparedAppResponse response = setupApi.registerWithApp();

        AppApi.enableGroupSync(response.jwt(), response.appId());
        String oldName = rDepartmentName();
        String departmentId = DepartmentApi.createDepartment(response.jwt(), oldName);
        assertEquals(oldName, groupRepository.byDepartmentIdOptional(departmentId, response.appId()).get().getName());

        String newName = rDepartmentName();
        DepartmentApi.renameDepartment(response.jwt(), departmentId, RenameDepartmentCommand.builder().name(newName).build());
        assertEquals(newName, groupRepository.byDepartmentIdOptional(departmentId, response.appId()).get().getName());
    }

    @Test
    public void should_rename_department_with_same_name_but_different_level() {
        LoginResponse response = setupApi.registerWithLogin();
        String name = rDepartmentName();
        String departmentId = DepartmentApi.createDepartment(response.jwt(), name);
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(response.jwt(), departmentId, rDepartmentName());

        DepartmentApi.renameDepartment(response.jwt(), subDepartmentId, RenameDepartmentCommand.builder().name(name).build());

        assertEquals(name, departmentRepository.byId(subDepartmentId).getName());
    }

    @Test
    public void should_fail_rename_department_if_name_already_exist_at_same_level() {
        LoginResponse response = setupApi.registerWithLogin();
        String name = rDepartmentName();
        String departmentId1 = DepartmentApi.createDepartment(response.jwt(), name);
        String departmentId2 = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());
        assertError(
                () -> DepartmentApi.renameDepartmentRaw(response.jwt(), departmentId2, RenameDepartmentCommand.builder().name(name).build()),
                DEPARTMENT_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_add_department_manager() {
        LoginResponse response = setupApi.registerWithLogin();
        String memberId = MemberApi.createMember(response.jwt(), rMemberName(), rMobile(), rPassword());
        String departmentId = DepartmentApi.createDepartment(response.jwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.jwt(), memberId, command);

        DepartmentApi.addDepartmentManager(response.jwt(), departmentId, memberId);
        Department department = departmentRepository.byId(departmentId);
        assertTrue(department.getManagers().contains(memberId));

        DepartmentManagersChangedEvent event = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());
    }

    @Test
    public void department_manager_change_should_sync_to_group() {
        PreparedAppResponse response = setupApi.registerWithApp();

        AppApi.enableGroupSync(response.jwt(), response.appId());
        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.jwt(), response.memberId(), command);
        DepartmentApi.addDepartmentManager(response.jwt(), departmentId, response.memberId());

        Group group = groupRepository.byDepartmentIdOptional(departmentId, response.appId()).get();
        assertTrue(group.getManagers().contains(response.memberId()));
    }

    @Test
    public void should_fail_add_department_manager_if_not_a_department_member() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId = DepartmentApi.createDepartment(response.jwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());

        assertError(() -> DepartmentApi.addDepartmentManagerRaw(response.jwt(), departmentId, response.memberId()),
                NOT_DEPARTMENT_MEMBER);
    }

    @Test
    public void should_remove_department_manager() {
        LoginResponse response = setupApi.registerWithLogin();
        String memberId = MemberApi.createMember(response.jwt(), rMemberName(), rMobile(), rPassword());
        String departmentId = DepartmentApi.createDepartment(response.jwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.jwt(), memberId, command);

        DepartmentApi.addDepartmentManager(response.jwt(), departmentId, memberId);
        assertTrue(departmentRepository.byId(departmentId).getManagers().contains(memberId));
        DepartmentManagersChangedEvent event = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());

        DepartmentApi.removeDepartmentManager(response.jwt(), departmentId, memberId);
        assertFalse(departmentRepository.byId(departmentId).getManagers().contains(memberId));

        DepartmentManagersChangedEvent updatedEvent = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED,
                DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, updatedEvent.getDepartmentId());
        assertNotEquals(event.getId(), updatedEvent.getId());
    }

    @Test
    public void should_delete_department() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId = DepartmentApi.createDepartment(response.jwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        assertEquals(1, tenantRepository.byId(response.tenantId()).getResourceUsage().getDepartmentCount());
        assertTrue(departmentHierarchyRepository.byTenantId(response.tenantId()).containsDepartmentId(departmentId));

        DepartmentApi.deleteDepartment(response.jwt(), departmentId);
        assertFalse(departmentRepository.exists(departmentId));
        assertEquals(0, tenantRepository.byId(response.tenantId()).getResourceUsage().getDepartmentCount());

        DepartmentDeletedEvent event = latestEventFor(departmentId, DEPARTMENT_DELETED, DepartmentDeletedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(response.tenantId());
        assertFalse(departmentHierarchy.containsDepartmentId(departmentId));

        assertEquals(response.tenantId(),
                latestEventFor(departmentHierarchy.getId(), DEPARTMENT_HIERARCHY_CHANGED, DepartmentHierarchyChangedEvent.class).getTenantId());
    }

    @Test
    public void delete_department_should_also_remove_it_from_member_departments() {
        LoginResponse response = setupApi.registerWithLogin();
        String departmentId = DepartmentApi.createDepartment(response.jwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        String memberId = MemberApi.createMember(response.jwt(), CreateMemberCommand.builder()
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .mobile(rMobile())
                .password(rPassword())
                .build());

        assertTrue(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));
        DepartmentApi.deleteDepartment(response.jwt(), departmentId);
        assertFalse(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));
    }

    @Test
    public void delete_department_should_also_delete_sub_departments() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(response.jwt(), departmentId, rDepartmentName());

        DepartmentApi.deleteDepartment(response.jwt(), departmentId);
        assertFalse(departmentRepository.exists(subDepartmentId));
        assertEquals(subDepartmentId, latestEventFor(subDepartmentId, DEPARTMENT_DELETED, DepartmentDeletedEvent.class).getDepartmentId());
        assertEquals(departmentId, latestEventFor(departmentId, DEPARTMENT_DELETED, DepartmentDeletedEvent.class).getDepartmentId());
    }

    @Test
    public void delete_department_should_also_un_sync_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.enableGroupSync(response.jwt(), response.appId());
        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(response.jwt(), departmentId, rDepartmentName());
        assertTrue(groupRepository.byDepartmentIdOptional(departmentId, response.appId()).isPresent());
        assertTrue(groupRepository.byDepartmentIdOptional(subDepartmentId, response.appId()).isPresent());
        DepartmentApi.deleteDepartment(response.jwt(), departmentId);
        assertFalse(groupRepository.byDepartmentIdOptional(departmentId, response.appId()).isPresent());
        assertFalse(groupRepository.byDepartmentIdOptional(subDepartmentId, response.appId()).isPresent());
    }

    @Test
    public void delete_department_should_also_evict_member_cache() {
        LoginResponse response = setupApi.registerWithLogin();
        String departmentId = DepartmentApi.createDepartment(response.jwt(), rDepartmentName());
        String memberId = MemberApi.createMemberUnderDepartment(response.jwt(), departmentId);
        assertNotNull(memberRepository.cachedById(response.memberId()));
        assertNotNull(memberRepository.cachedById(memberId));
        assertNotNull(memberRepository.cachedTenantAllMembers(response.tenantId()));
        String membersKey = "Cache:TENANT_MEMBERS::" + response.tenantId();
        String memberKey = "Cache:MEMBER::" + response.memberId();
        String newMemberKey = "Cache:MEMBER::" + memberId;
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(membersKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(memberKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(newMemberKey)));

        DepartmentApi.deleteDepartment(response.jwt(), departmentId);
        PollingAssertion.pollAssert().run(() -> assertEquals(FALSE, stringRedisTemplate.hasKey(membersKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(memberKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(FALSE, stringRedisTemplate.hasKey(newMemberKey)));
    }

    @Test
    public void should_cache_departments() {
        LoginResponse response = setupApi.registerWithLogin();
        DepartmentApi.createDepartment(response.jwt(), CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        String key = "Cache:TENANT_DEPARTMENTS::" + response.tenantId();

        PollingAssertion.pollAssert().run(() -> assertEquals(FALSE, stringRedisTemplate.hasKey(key)));

        DepartmentHierarchyApi.fetchDepartmentHierarchy(response.jwt());
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(key)));

        DepartmentApi.createDepartment(response.jwt(), CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        PollingAssertion.pollAssert().run(() -> assertEquals(FALSE, stringRedisTemplate.hasKey(key)));
    }
}
