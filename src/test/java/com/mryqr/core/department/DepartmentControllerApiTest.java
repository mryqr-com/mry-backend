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
        String departmentId = DepartmentApi.createDepartment(response.getJwt(), CreateDepartmentCommand.builder().name(departmentName).build());

        Department department = departmentRepository.byId(departmentId);
        assertEquals(departmentName, department.getName());

        Tenant tenant = tenantRepository.byId(department.getTenantId());
        assertEquals(1, tenant.getResourceUsage().getDepartmentCount());

        DepartmentCreatedEvent event = latestEventFor(departmentId, DEPARTMENT_CREATED, DepartmentCreatedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(response.getTenantId());
        assertEquals(1, departmentHierarchy.allDepartmentIds().size());
        assertTrue(departmentHierarchy.containsDepartmentId(departmentId));

        assertEquals(response.getTenantId(),
                latestEventFor(departmentHierarchy.getId(), DEPARTMENT_HIERARCHY_CHANGED, DepartmentHierarchyChangedEvent.class).getTenantId());
    }

    @Test
    public void should_create_department_with_parent() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String parentDepartmentId = DepartmentApi.createDepartment(loginResponse.getJwt(), rDepartmentName());
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(loginResponse.getJwt(), parentDepartmentId, rDepartmentName());

        Department department = departmentRepository.byId(subDepartmentId);
        assertNotNull(department);
        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(loginResponse.getTenantId());
        departmentHierarchy.containsDepartmentId(parentDepartmentId);
        departmentHierarchy.containsDepartmentId(subDepartmentId);
    }

    @Test
    public void should_create_department_with_same_name_but_different_level() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String parentName = rDepartmentName();
        String childName = rDepartmentName();
        String parentDepartmentId = DepartmentApi.createDepartment(loginResponse.getJwt(), parentName);
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(loginResponse.getJwt(), parentDepartmentId, childName);
        assertEquals(parentName, departmentRepository.byId(parentDepartmentId).getName());
        assertEquals(childName, departmentRepository.byId(subDepartmentId).getName());
        List<TenantCachedDepartment> tenantCachedDepartments = departmentRepository.cachedTenantAllDepartments(loginResponse.getTenantId());
        assertEquals(2, tenantCachedDepartments.size());
    }

    @Test
    public void create_department_should_also_sync_to_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.enableGroupSync(response.getJwt(), response.getAppId());

        String departmentId = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.getJwt(), response.getMemberId(), command);
        DepartmentApi.addDepartmentManager(response.getJwt(), departmentId, response.getMemberId());
        DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());//trigger sync again to sync members
        Optional<Group> group = groupRepository.byDepartmentIdOptional(departmentId, response.getAppId());
        assertTrue(group.isPresent());
        assertTrue(group.get().getManagers().contains(response.getMemberId()));
        assertTrue(group.get().getMembers().contains(response.getMemberId()));
    }

    @Test
    public void should_fail_create_department_if_parent_department_not_exist() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        assertError(() -> DepartmentApi.createDepartmentRaw(loginResponse.getJwt(),
                        CreateDepartmentCommand.builder().name(rDepartmentName()).parentDepartmentId(Department.newDepartmentId()).build()),
                DEPARTMENT_NOT_FOUND);
    }

    @Test
    public void should_fail_create_department_if_hierarchy_too_deep() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        String departmentId1 = DepartmentApi.createDepartment(loginResponse.getJwt(), rDepartmentName());
        String departmentId2 = DepartmentApi.createDepartmentWithParent(loginResponse.getJwt(), departmentId1, rDepartmentName());
        String departmentId3 = DepartmentApi.createDepartmentWithParent(loginResponse.getJwt(), departmentId2, rDepartmentName());
        String departmentId4 = DepartmentApi.createDepartmentWithParent(loginResponse.getJwt(), departmentId3, rDepartmentName());
        String departmentId5 = DepartmentApi.createDepartmentWithParent(loginResponse.getJwt(), departmentId4, rDepartmentName());
        assertError(() -> DepartmentApi.createDepartmentRaw(loginResponse.getJwt(),
                        CreateDepartmentCommand.builder().name(rDepartmentName()).parentDepartmentId(departmentId5).build()),
                DEPARTMENT_HIERARCHY_TOO_DEEP);
    }

    @Test
    public void should_fail_create_department_if_max_limit_reached() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        Tenant theTenant = tenantRepository.byId(loginResponse.getTenantId());
        theTenant.setDepartmentCount(theTenant.currentPlan().getMaxDepartmentCount());
        tenantRepository.save(theTenant);
        assertError(
                () -> DepartmentApi.createDepartmentRaw(loginResponse.getJwt(), CreateDepartmentCommand.builder().name(rDepartmentName()).build()),
                DEPARTMENT_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_fail_create_department_if_name_already_exists() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateDepartmentCommand command = CreateDepartmentCommand.builder().name(rDepartmentName()).build();
        DepartmentApi.createDepartment(response.getJwt(), command);

        assertError(() -> DepartmentApi.createDepartmentRaw(response.getJwt(), command), DEPARTMENT_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_department_if_exceed_max_count() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        ResourceUsage resourceUsage = tenant.getResourceUsage();
        ReflectionTestUtils.setField(resourceUsage, "departmentCount", tenant.getPackages().effectiveMaxDepartmentCount());
        tenantRepository.save(tenant);

        CreateDepartmentCommand command = CreateDepartmentCommand.builder().name(rDepartmentName()).build();
        assertError(() -> DepartmentApi.createDepartmentRaw(response.getJwt(), command), DEPARTMENT_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_rename_department() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId = DepartmentApi.createDepartment(response.getJwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        String newName = rDepartmentName();

        DepartmentApi.renameDepartment(response.getJwt(), departmentId, RenameDepartmentCommand.builder().name(newName).build());

        Department department = departmentRepository.byId(departmentId);
        assertEquals(newName, department.getName());

        DepartmentRenamedEvent event = latestEventFor(departmentId, DEPARTMENT_RENAMED, DepartmentRenamedEvent.class);
        assertEquals(department.getId(), event.getDepartmentId());
    }

    @Test
    public void rename_department_should_sync_to_group() {
        PreparedAppResponse response = setupApi.registerWithApp();

        AppApi.enableGroupSync(response.getJwt(), response.getAppId());
        String oldName = rDepartmentName();
        String departmentId = DepartmentApi.createDepartment(response.getJwt(), oldName);
        assertEquals(oldName, groupRepository.byDepartmentIdOptional(departmentId, response.getAppId()).get().getName());

        String newName = rDepartmentName();
        DepartmentApi.renameDepartment(response.getJwt(), departmentId, RenameDepartmentCommand.builder().name(newName).build());
        assertEquals(newName, groupRepository.byDepartmentIdOptional(departmentId, response.getAppId()).get().getName());
    }

    @Test
    public void should_rename_department_with_same_name_but_different_level() {
        LoginResponse response = setupApi.registerWithLogin();
        String name = rDepartmentName();
        String departmentId = DepartmentApi.createDepartment(response.getJwt(), name);
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(response.getJwt(), departmentId, rDepartmentName());

        DepartmentApi.renameDepartment(response.getJwt(), subDepartmentId, RenameDepartmentCommand.builder().name(name).build());

        assertEquals(name, departmentRepository.byId(subDepartmentId).getName());
    }

    @Test
    public void should_fail_rename_department_if_name_already_exist_at_same_level() {
        LoginResponse response = setupApi.registerWithLogin();
        String name = rDepartmentName();
        String departmentId1 = DepartmentApi.createDepartment(response.getJwt(), name);
        String departmentId2 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        assertError(
                () -> DepartmentApi.renameDepartmentRaw(response.getJwt(), departmentId2, RenameDepartmentCommand.builder().name(name).build()),
                DEPARTMENT_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_add_department_manager() {
        LoginResponse response = setupApi.registerWithLogin();
        String memberId = MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), rPassword());
        String departmentId = DepartmentApi.createDepartment(response.getJwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.getJwt(), memberId, command);

        DepartmentApi.addDepartmentManager(response.getJwt(), departmentId, memberId);
        Department department = departmentRepository.byId(departmentId);
        assertTrue(department.getManagers().contains(memberId));

        DepartmentManagersChangedEvent event = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());
    }

    @Test
    public void department_manager_change_should_sync_to_group() {
        PreparedAppResponse response = setupApi.registerWithApp();

        AppApi.enableGroupSync(response.getJwt(), response.getAppId());
        String departmentId = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.getJwt(), response.getMemberId(), command);
        DepartmentApi.addDepartmentManager(response.getJwt(), departmentId, response.getMemberId());

        Group group = groupRepository.byDepartmentIdOptional(departmentId, response.getAppId()).get();
        assertTrue(group.getManagers().contains(response.getMemberId()));
    }

    @Test
    public void should_fail_add_department_manager_if_not_a_department_member() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId = DepartmentApi.createDepartment(response.getJwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());

        assertError(() -> DepartmentApi.addDepartmentManagerRaw(response.getJwt(), departmentId, response.getMemberId()),
                NOT_DEPARTMENT_MEMBER);
    }

    @Test
    public void should_remove_department_manager() {
        LoginResponse response = setupApi.registerWithLogin();
        String memberId = MemberApi.createMember(response.getJwt(), rMemberName(), rMobile(), rPassword());
        String departmentId = DepartmentApi.createDepartment(response.getJwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());

        UpdateMemberInfoCommand command = UpdateMemberInfoCommand.builder()
                .mobile(rMobile()).email(rEmail())
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .build();

        MemberApi.updateMember(response.getJwt(), memberId, command);

        DepartmentApi.addDepartmentManager(response.getJwt(), departmentId, memberId);
        assertTrue(departmentRepository.byId(departmentId).getManagers().contains(memberId));
        DepartmentManagersChangedEvent event = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED, DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());

        DepartmentApi.removeDepartmentManager(response.getJwt(), departmentId, memberId);
        assertFalse(departmentRepository.byId(departmentId).getManagers().contains(memberId));

        DepartmentManagersChangedEvent updatedEvent = latestEventFor(departmentId, DEPARTMENT_MANAGERS_CHANGED,
                DepartmentManagersChangedEvent.class);
        assertEquals(departmentId, updatedEvent.getDepartmentId());
        assertNotEquals(event.getId(), updatedEvent.getId());
    }

    @Test
    public void should_delete_department() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId = DepartmentApi.createDepartment(response.getJwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        assertEquals(1, tenantRepository.byId(response.getTenantId()).getResourceUsage().getDepartmentCount());
        assertTrue(departmentHierarchyRepository.byTenantId(response.getTenantId()).containsDepartmentId(departmentId));

        DepartmentApi.deleteDepartment(response.getJwt(), departmentId);
        assertFalse(departmentRepository.exists(departmentId));
        assertEquals(0, tenantRepository.byId(response.getTenantId()).getResourceUsage().getDepartmentCount());

        DepartmentDeletedEvent event = latestEventFor(departmentId, DEPARTMENT_DELETED, DepartmentDeletedEvent.class);
        assertEquals(departmentId, event.getDepartmentId());

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(response.getTenantId());
        assertFalse(departmentHierarchy.containsDepartmentId(departmentId));

        assertEquals(response.getTenantId(),
                latestEventFor(departmentHierarchy.getId(), DEPARTMENT_HIERARCHY_CHANGED, DepartmentHierarchyChangedEvent.class).getTenantId());
    }

    @Test
    public void delete_department_should_also_remove_it_from_member_departments() {
        LoginResponse response = setupApi.registerWithLogin();
        String departmentId = DepartmentApi.createDepartment(response.getJwt(),
                CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        String memberId = MemberApi.createMember(response.getJwt(), CreateMemberCommand.builder()
                .name(rMemberName())
                .departmentIds(List.of(departmentId))
                .mobile(rMobile())
                .password(rPassword())
                .build());

        assertTrue(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));
        DepartmentApi.deleteDepartment(response.getJwt(), departmentId);
        assertFalse(memberRepository.byId(memberId).getDepartmentIds().contains(departmentId));
    }

    @Test
    public void delete_department_should_also_delete_sub_departments() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(response.getJwt(), departmentId, rDepartmentName());

        DepartmentApi.deleteDepartment(response.getJwt(), departmentId);
        assertFalse(departmentRepository.exists(subDepartmentId));
        assertEquals(subDepartmentId, latestEventFor(subDepartmentId, DEPARTMENT_DELETED, DepartmentDeletedEvent.class).getDepartmentId());
        assertEquals(departmentId, latestEventFor(departmentId, DEPARTMENT_DELETED, DepartmentDeletedEvent.class).getDepartmentId());
    }

    @Test
    public void delete_department_should_also_un_sync_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.enableGroupSync(response.getJwt(), response.getAppId());
        String departmentId = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String subDepartmentId = DepartmentApi.createDepartmentWithParent(response.getJwt(), departmentId, rDepartmentName());
        assertTrue(groupRepository.byDepartmentIdOptional(departmentId, response.getAppId()).isPresent());
        assertTrue(groupRepository.byDepartmentIdOptional(subDepartmentId, response.getAppId()).isPresent());
        DepartmentApi.deleteDepartment(response.getJwt(), departmentId);
        assertFalse(groupRepository.byDepartmentIdOptional(departmentId, response.getAppId()).isPresent());
        assertFalse(groupRepository.byDepartmentIdOptional(subDepartmentId, response.getAppId()).isPresent());
    }

    @Test
    public void delete_department_should_also_evict_member_cache() {
        LoginResponse response = setupApi.registerWithLogin();
        String departmentId = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String memberId = MemberApi.createMemberUnderDepartment(response.getJwt(), departmentId);
        assertNotNull(memberRepository.cachedById(response.getMemberId()));
        assertNotNull(memberRepository.cachedById(memberId));
        assertNotNull(memberRepository.cachedTenantAllMembers(response.getTenantId()));
        String membersKey = "Cache:TENANT_MEMBERS::" + response.getTenantId();
        String memberKey = "Cache:MEMBER::" + response.getMemberId();
        String newMemberKey = "Cache:MEMBER::" + memberId;
        assertEquals(TRUE, stringRedisTemplate.hasKey(membersKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(memberKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(newMemberKey));

        DepartmentApi.deleteDepartment(response.getJwt(), departmentId);
        assertEquals(FALSE, stringRedisTemplate.hasKey(membersKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(memberKey));
        assertEquals(FALSE, stringRedisTemplate.hasKey(newMemberKey));
    }

    @Test
    public void should_cache_departments() {
        LoginResponse response = setupApi.registerWithLogin();
        DepartmentApi.createDepartment(response.getJwt(), CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        String key = "Cache:TENANT_DEPARTMENTS::" + response.getTenantId();

        assertEquals(FALSE, stringRedisTemplate.hasKey(key));

        DepartmentHierarchyApi.fetchDepartmentHierarchy(response.getJwt());
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        DepartmentApi.createDepartment(response.getJwt(), CreateDepartmentCommand.builder().name(rDepartmentName()).build());
        assertEquals(FALSE, stringRedisTemplate.hasKey(key));
    }
}
