package com.mryqr.core.grouphierarchy;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.common.domain.idnode.IdTree;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.group.command.CreateGroupCommand;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.grouphierarchy.command.UpdateGroupHierarchyCommand;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.query.QGroupHierarchy;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.core.common.exception.ErrorCode.GROUP_HIERARCHY_NOT_MATCH;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_HIERARCHY_TOO_DEEP;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_NAME_DUPLICATES;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.utils.RandomTestFixture.rAppName;
import static com.mryqr.utils.RandomTestFixture.rGroupName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GroupHierarchyControllerApiTest extends BaseApiTest {

    @Test
    public void should_fetch_group_hierarchy() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).getJwt();
        CreateAppResponse appResponse = AppApi.createApp(jwt, rAppName());

        QGroupHierarchy hierarchy = GroupHierarchyApi.fetchGroupHierarchy(jwt, appResponse.getAppId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(appResponse.getAppId());
        assertEquals(groupHierarchy.getIdTree(), hierarchy.getIdTree());
        assertTrue(hierarchy.getAllGroups().stream().map(QGroupHierarchy.QHierarchyGroup::getId).toList().contains(appResponse.getDefaultGroupId()));
    }

    @Test
    public void should_update_group_hierarchy() {
        PreparedAppResponse response = setupApi.registerWithApp();

        CreateGroupCommand command = CreateGroupCommand.builder()
                .name(rGroupName())
                .parentGroupId(response.getDefaultGroupId())
                .appId(response.getAppId())
                .build();

        String groupId = GroupApi.createGroup(response.getJwt(), command);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(response.getAppId());
        assertEquals(groupHierarchy.getHierarchy().schemaOf(groupId), response.getDefaultGroupId() + "/" + groupId);
        assertEquals(groupHierarchy.getHierarchy().schemaOf(response.getDefaultGroupId()), response.getDefaultGroupId());

        IdTree idTree = new IdTree(response.getDefaultGroupId());
        idTree.addNode(null, groupId);
        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();

        GroupHierarchyApi.updateGroupHierarchy(response.getJwt(), response.getAppId(), updateCommand);

        GroupHierarchy updated = groupHierarchyRepository.byAppId(response.getAppId());
        assertEquals(updated.getHierarchy().schemaOf(response.getDefaultGroupId()), response.getDefaultGroupId());
        assertEquals(updated.getHierarchy().schemaOf(groupId), groupId);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_group_not_exists() {
        PreparedAppResponse response = setupApi.registerWithApp();
        IdTree idTree = new IdTree(response.getDefaultGroupId());
        idTree.addNode(null, Group.newGroupId());
        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();
        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.getJwt(), response.getAppId(), updateCommand), GROUP_HIERARCHY_NOT_MATCH);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_not_all_group_provided() {
        PreparedAppResponse response = setupApi.registerWithApp();

        CreateGroupCommand command = CreateGroupCommand.builder()
                .name(rGroupName())
                .parentGroupId(response.getDefaultGroupId())
                .appId(response.getAppId())
                .build();

        String groupId = GroupApi.createGroup(response.getJwt(), command);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(response.getAppId());
        assertEquals(groupHierarchy.getHierarchy().schemaOf(groupId), response.getDefaultGroupId() + "/" + groupId);
        assertEquals(groupHierarchy.getHierarchy().schemaOf(response.getDefaultGroupId()), response.getDefaultGroupId());

        IdTree idTree = new IdTree(response.getDefaultGroupId());
        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();
        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.getJwt(), response.getAppId(), updateCommand), GROUP_HIERARCHY_NOT_MATCH);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_too_deep() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String groupId1 = GroupApi.createGroup(response.getJwt(), response.getAppId());
        String groupId2 = GroupApi.createGroup(response.getJwt(), response.getAppId());
        String groupId3 = GroupApi.createGroup(response.getJwt(), response.getAppId());
        String groupId4 = GroupApi.createGroup(response.getJwt(), response.getAppId());
        String groupId5 = GroupApi.createGroup(response.getJwt(), response.getAppId());

        IdTree idTree = new IdTree(response.getDefaultGroupId());
        idTree.addNode(response.getDefaultGroupId(), groupId1);
        idTree.addNode(groupId1, groupId2);
        idTree.addNode(groupId2, groupId3);
        idTree.addNode(groupId3, groupId4);
        idTree.addNode(groupId4, groupId5);

        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();

        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.getJwt(), response.getAppId(), updateCommand), GROUP_HIERARCHY_TOO_DEEP);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_name_duplicates_at_root_level() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String groupName = rGroupName();
        String groupId1 = GroupApi.createGroup(response.getJwt(), response.getAppId(), groupName);
        CreateGroupCommand command = CreateGroupCommand.builder().parentGroupId(groupId1).name(groupName).appId(response.getAppId()).build();
        String groupId2 = GroupApi.createGroup(response.getJwt(), command);

        IdTree idTree = new IdTree(response.getDefaultGroupId());
        idTree.addNode(null, groupId1);
        idTree.addNode(null, groupId2);

        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();

        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.getJwt(), response.getAppId(), updateCommand), GROUP_NAME_DUPLICATES);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_name_duplicates_at_none_root_level() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), PROFESSIONAL);

        String groupName = rGroupName();

        String groupId1 = GroupApi.createGroup(response.getJwt(), CreateGroupCommand.builder().parentGroupId(response.getDefaultGroupId()).name(groupName).appId(response.getAppId()).build());
        String groupId2 = GroupApi.createGroup(response.getJwt(), CreateGroupCommand.builder().parentGroupId(groupId1).name(groupName).appId(response.getAppId()).build());

        IdTree idTree = new IdTree(response.getDefaultGroupId());
        idTree.addNode(response.getDefaultGroupId(), groupId1);
        idTree.addNode(response.getDefaultGroupId(), groupId2);

        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();

        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.getJwt(), response.getAppId(), updateCommand), GROUP_NAME_DUPLICATES);
    }

    @Test
    public void should_cache_group_hierarchy() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String key = "Cache:GROUP_HIERARCHY::" + response.getAppId();
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));

        groupHierarchyRepository.cachedByAppId(response.getAppId());
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(response.getAppId());
        groupHierarchyRepository.save(groupHierarchy);
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
    }

}
