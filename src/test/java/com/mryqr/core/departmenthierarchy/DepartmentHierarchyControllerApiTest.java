package com.mryqr.core.departmenthierarchy;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.idnode.IdTree;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.department.DepartmentApi;
import com.mryqr.core.department.command.RenameDepartmentCommand;
import com.mryqr.core.department.domain.Department;
import com.mryqr.core.departmenthierarchy.command.UpdateDepartmentHierarchyCommand;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.event.DepartmentHierarchyChangedEvent;
import com.mryqr.core.departmenthierarchy.query.QDepartmentHierarchy;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.utils.LoginResponse;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.mryqr.common.event.DomainEventType.DEPARTMENT_HIERARCHY_CHANGED;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.utils.RandomTestFixture.rDepartmentName;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

public class DepartmentHierarchyControllerApiTest extends BaseApiTest {

    @Test
    public void should_fetch_department_hierarchy() {
        LoginResponse response = setupApi.registerWithLogin();
        String departmentId1 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId2 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId3 = DepartmentApi.createDepartmentWithParent(response.getJwt(), departmentId1, rDepartmentName());

        QDepartmentHierarchy hierarchy = DepartmentHierarchyApi.fetchDepartmentHierarchy(response.getJwt());
        List<String> departmentIds = hierarchy.getAllDepartments().stream().map(QDepartmentHierarchy.QHierarchyDepartment::getId).toList();
        assertEquals(3, departmentIds.size());
        assertTrue(departmentIds.containsAll(List.of(departmentId1, departmentId2, departmentId3)));

        DepartmentHierarchy departmentHierarchy = departmentHierarchyRepository.byTenantId(response.getTenantId());
        assertEquals(departmentHierarchy.getIdTree(), hierarchy.getIdTree());
    }

    @Test
    public void should_update_department_hierarchy() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId1 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId2 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId3 = DepartmentApi.createDepartmentWithParent(response.getJwt(), departmentId1, rDepartmentName());

        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, departmentId2);
        idTree.addNode(null, departmentId3);
        idTree.addNode(departmentId2, departmentId1);

        UpdateDepartmentHierarchyCommand command = UpdateDepartmentHierarchyCommand.builder().idTree(idTree).build();
        DepartmentHierarchyApi.updateDepartmentHierarchy(response.getJwt(), command);

        DepartmentHierarchy hierarchy = departmentHierarchyRepository.byTenantId(response.getTenantId());
        assertEquals(3, hierarchy.allDepartmentIds().size());
        assertTrue(hierarchy.allDepartmentIds().containsAll(List.of(departmentId1, departmentId2, departmentId3)));
        assertEquals(departmentId2 + "/" + departmentId1, hierarchy.getHierarchy().schemaOf(departmentId1));
        assertEquals(departmentId2, hierarchy.getHierarchy().schemaOf(departmentId2));
        assertEquals(departmentId3, hierarchy.getHierarchy().schemaOf(departmentId3));

        assertEquals(response.getTenantId(),
                latestEventFor(hierarchy.getId(), DEPARTMENT_HIERARCHY_CHANGED, DepartmentHierarchyChangedEvent.class).getTenantId());
    }

    @Test
    public void update_hierarchy_should_also_sync_to_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        AppApi.enableGroupSync(response.getJwt(), response.getAppId());
        String departmentId1 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId2 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId3 = DepartmentApi.createDepartmentWithParent(response.getJwt(), departmentId1, rDepartmentName());

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(response.getAppId());
        Group group1 = groupRepository.byDepartmentIdOptional(departmentId1, response.getAppId()).get();
        Group group2 = groupRepository.byDepartmentIdOptional(departmentId2, response.getAppId()).get();
        Group group3 = groupRepository.byDepartmentIdOptional(departmentId3, response.getAppId()).get();
        assertEquals(group1.getId(), groupHierarchy.getHierarchy().schemaOf(group1.getId()));
        assertEquals(group2.getId(), groupHierarchy.getHierarchy().schemaOf(group2.getId()));
        assertEquals(group1.getId() + "/" + group3.getId(), groupHierarchy.getHierarchy().schemaOf(group3.getId()));

        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, departmentId2);
        idTree.addNode(null, departmentId3);
        idTree.addNode(departmentId2, departmentId1);

        UpdateDepartmentHierarchyCommand command = UpdateDepartmentHierarchyCommand.builder().idTree(idTree).build();
        DepartmentHierarchyApi.updateDepartmentHierarchy(response.getJwt(), command);

        GroupHierarchy updatedHierarchy = groupHierarchyRepository.byAppId(response.getAppId());
        assertEquals(group2.getId() + "/" + group1.getId(), updatedHierarchy.getHierarchy().schemaOf(group1.getId()));
        assertEquals(group2.getId(), updatedHierarchy.getHierarchy().schemaOf(group2.getId()));
        assertEquals(group3.getId(), updatedHierarchy.getHierarchy().schemaOf(group3.getId()));
    }

    @Test
    public void should_fail_update_department_hierarchy_if_department_not_exists() {
        LoginResponse response = setupApi.registerWithLogin();
        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, Department.newDepartmentId());
        UpdateDepartmentHierarchyCommand updateCommand = UpdateDepartmentHierarchyCommand.builder()
                .idTree(idTree)
                .build();
        assertError(() -> DepartmentHierarchyApi.updateDepartmentHierarchyRaw(response.getJwt(), updateCommand),
                DEPARTMENT_HIERARCHY_NOT_MATCH);
    }

    @Test
    public void should_fail_update_department_hierarchy_if_not_all_department_provided() {
        LoginResponse response = setupApi.registerWithLogin();
        String departmentId1 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId2 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());

        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, departmentId1);
        UpdateDepartmentHierarchyCommand updateCommand = UpdateDepartmentHierarchyCommand.builder()
                .idTree(idTree)
                .build();
        assertError(() -> DepartmentHierarchyApi.updateDepartmentHierarchyRaw(response.getJwt(), updateCommand),
                DEPARTMENT_HIERARCHY_NOT_MATCH);
    }

    @Test
    public void should_fail_update_department_hierarchy_if_too_deep() {
        LoginResponse response = setupApi.registerWithLogin();

        String departmentId1 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId2 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId3 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId4 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId5 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId6 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, departmentId1);
        idTree.addNode(departmentId1, departmentId2);
        idTree.addNode(departmentId2, departmentId3);
        idTree.addNode(departmentId3, departmentId4);
        idTree.addNode(departmentId4, departmentId5);
        idTree.addNode(departmentId5, departmentId6);

        UpdateDepartmentHierarchyCommand updateCommand = UpdateDepartmentHierarchyCommand.builder()
                .idTree(idTree)
                .build();
        assertError(() -> DepartmentHierarchyApi.updateDepartmentHierarchyRaw(response.getJwt(), updateCommand), DEPARTMENT_HIERARCHY_TOO_DEEP);
    }

    @Test
    public void should_fail_update_department_hierarchy_if_name_duplicates_at_root_level() {
        LoginResponse response = setupApi.registerWithLogin();

        String name = rDepartmentName();
        String departmentId1 = DepartmentApi.createDepartment(response.getJwt(), name);
        String departmentId2 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());

        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, departmentId1);
        idTree.addNode(departmentId1, departmentId2);
        DepartmentHierarchyApi.updateDepartmentHierarchy(response.getJwt(), UpdateDepartmentHierarchyCommand.builder().idTree(idTree).build());
        DepartmentApi.renameDepartment(response.getJwt(), departmentId2, RenameDepartmentCommand.builder().name(name).build());

        IdTree updateIdTree = new IdTree(new ArrayList<>(0));
        updateIdTree.addNode(null, departmentId1);
        updateIdTree.addNode(null, departmentId2);

        UpdateDepartmentHierarchyCommand updateCommand = UpdateDepartmentHierarchyCommand.builder()
                .idTree(updateIdTree)
                .build();
        assertError(() -> DepartmentHierarchyApi.updateDepartmentHierarchyRaw(response.getJwt(), updateCommand), DEPARTMENT_NAME_DUPLICATES);
    }

    @Test
    public void should_fail_update_department_hierarchy_if_name_duplicates_at_none_root_level() {
        LoginResponse response = setupApi.registerWithLogin();

        String name = rDepartmentName();
        String departmentId1 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());
        String departmentId2 = DepartmentApi.createDepartment(response.getJwt(), name);
        String departmentId3 = DepartmentApi.createDepartment(response.getJwt(), rDepartmentName());

        IdTree idTree = new IdTree(new ArrayList<>(0));
        idTree.addNode(null, departmentId1);
        idTree.addNode(departmentId1, departmentId2);
        idTree.addNode(departmentId2, departmentId3);
        DepartmentHierarchyApi.updateDepartmentHierarchy(response.getJwt(), UpdateDepartmentHierarchyCommand.builder().idTree(idTree).build());
        DepartmentApi.renameDepartment(response.getJwt(), departmentId3, RenameDepartmentCommand.builder().name(name).build());

        IdTree updateIdTree = new IdTree(new ArrayList<>(0));
        updateIdTree.addNode(null, departmentId1);
        updateIdTree.addNode(departmentId1, departmentId2);
        updateIdTree.addNode(departmentId1, departmentId3);

        UpdateDepartmentHierarchyCommand updateCommand = UpdateDepartmentHierarchyCommand.builder()
                .idTree(updateIdTree)
                .build();
        assertError(() -> DepartmentHierarchyApi.updateDepartmentHierarchyRaw(response.getJwt(), updateCommand), DEPARTMENT_NAME_DUPLICATES);
    }

    @Test
    public void should_cache_department_hierarchy() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String key = "Cache:DEPARTMENT_HIERARCHY::" + response.getTenantId();
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));

        departmentHierarchyRepository.cachedByTenantId(response.getTenantId());
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        DepartmentHierarchy hierarchy = departmentHierarchyRepository.byTenantId(response.getTenantId());
        departmentHierarchyRepository.save(hierarchy);
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
    }
}
