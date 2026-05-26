package com.mryqr.core.grouphierarchy;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.idnode.IdTree;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.CreateAppResponse;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.group.command.CreateGroupCommand;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.grouphierarchy.command.UpdateGroupHierarchyCommand;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.query.QGroupHierarchy;
import com.mryqr.support.PollingAssertion;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

public class GroupHierarchyControllerApiTest extends BaseApiTest {

    @Test
    public void should_fetch_group_hierarchy() {
        String jwt = setupApi.registerWithLogin(rMobile(), rPassword()).jwt();
        CreateAppResponse appResponse = AppApi.createApp(jwt, rAppName());

        QGroupHierarchy hierarchy = GroupHierarchyApi.fetchGroupHierarchy(jwt, appResponse.getAppId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(appResponse.getAppId());
        assertEquals(groupHierarchy.getIdTree(), hierarchy.getIdTree());
        assertTrue(
                hierarchy.getAllGroups().stream().map(QGroupHierarchy.QHierarchyGroup::getId).toList().contains(appResponse.getDefaultGroupId()));
    }

    @Test
    public void should_update_group_hierarchy() {
        PreparedAppResponse response = setupApi.registerWithApp();

        CreateGroupCommand command = CreateGroupCommand.builder()
                .name(rGroupName())
                .parentGroupId(response.defaultGroupId())
                .appId(response.appId())
                .build();

        String groupId = GroupApi.createGroup(response.jwt(), command);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(response.appId());
        assertEquals(groupHierarchy.getHierarchy().schemaOf(groupId), response.defaultGroupId() + "/" + groupId);
        assertEquals(groupHierarchy.getHierarchy().schemaOf(response.defaultGroupId()), response.defaultGroupId());

        IdTree idTree = new IdTree(response.defaultGroupId());
        idTree.addNode(null, groupId);
        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();

        GroupHierarchyApi.updateGroupHierarchy(response.jwt(), response.appId(), updateCommand);

        GroupHierarchy updated = groupHierarchyRepository.byAppId(response.appId());
        assertEquals(updated.getHierarchy().schemaOf(response.defaultGroupId()), response.defaultGroupId());
        assertEquals(updated.getHierarchy().schemaOf(groupId), groupId);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_group_not_exists() {
        PreparedAppResponse response = setupApi.registerWithApp();
        IdTree idTree = new IdTree(response.defaultGroupId());
        idTree.addNode(null, Group.newGroupId());
        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();
        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.jwt(), response.appId(), updateCommand),
                GROUP_HIERARCHY_NOT_MATCH);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_not_all_group_provided() {
        PreparedAppResponse response = setupApi.registerWithApp();

        CreateGroupCommand command = CreateGroupCommand.builder()
                .name(rGroupName())
                .parentGroupId(response.defaultGroupId())
                .appId(response.appId())
                .build();

        String groupId = GroupApi.createGroup(response.jwt(), command);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(response.appId());
        assertEquals(groupHierarchy.getHierarchy().schemaOf(groupId), response.defaultGroupId() + "/" + groupId);
        assertEquals(groupHierarchy.getHierarchy().schemaOf(response.defaultGroupId()), response.defaultGroupId());

        IdTree idTree = new IdTree(response.defaultGroupId());
        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();
        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.jwt(), response.appId(), updateCommand),
                GROUP_HIERARCHY_NOT_MATCH);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_too_deep() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupId1 = GroupApi.createGroup(response.jwt(), response.appId());
        String groupId2 = GroupApi.createGroup(response.jwt(), response.appId());
        String groupId3 = GroupApi.createGroup(response.jwt(), response.appId());
        String groupId4 = GroupApi.createGroup(response.jwt(), response.appId());
        String groupId5 = GroupApi.createGroup(response.jwt(), response.appId());

        IdTree idTree = new IdTree(response.defaultGroupId());
        idTree.addNode(response.defaultGroupId(), groupId1);
        idTree.addNode(groupId1, groupId2);
        idTree.addNode(groupId2, groupId3);
        idTree.addNode(groupId3, groupId4);
        idTree.addNode(groupId4, groupId5);

        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();

        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.jwt(), response.appId(), updateCommand),
                GROUP_HIERARCHY_TOO_DEEP);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_name_duplicates_at_root_level() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupName = rGroupName();
        String groupId1 = GroupApi.createGroup(response.jwt(), response.appId(), groupName);
        CreateGroupCommand command = CreateGroupCommand.builder().parentGroupId(groupId1).name(groupName).appId(response.appId()).build();
        String groupId2 = GroupApi.createGroup(response.jwt(), command);

        IdTree idTree = new IdTree(response.defaultGroupId());
        idTree.addNode(null, groupId1);
        idTree.addNode(null, groupId2);

        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();

        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.jwt(), response.appId(), updateCommand),
                GROUP_NAME_DUPLICATES);
    }

    @Test
    public void should_fail_update_group_hierarchy_if_name_duplicates_at_none_root_level() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupName = rGroupName();

        String groupId1 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().parentGroupId(response.defaultGroupId()).name(groupName).appId(response.appId()).build());
        String groupId2 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().parentGroupId(groupId1).name(groupName).appId(response.appId()).build());

        IdTree idTree = new IdTree(response.defaultGroupId());
        idTree.addNode(response.defaultGroupId(), groupId1);
        idTree.addNode(response.defaultGroupId(), groupId2);

        UpdateGroupHierarchyCommand updateCommand = UpdateGroupHierarchyCommand.builder()
                .idTree(idTree)
                .build();

        assertError(() -> GroupHierarchyApi.updateGroupHierarchyRaw(response.jwt(), response.appId(), updateCommand),
                GROUP_NAME_DUPLICATES);
    }

    @Test
    public void should_cache_group_hierarchy() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String key = "Cache:GROUP_HIERARCHY::" + response.appId();
        PollingAssertion.pollAssert().run(() ->      assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));

        groupHierarchyRepository.cachedByAppId(response.appId());
        PollingAssertion.pollAssert().run(() ->     assertEquals(TRUE, stringRedisTemplate.hasKey(key)));

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(response.appId());
        groupHierarchyRepository.save(groupHierarchy);
        PollingAssertion.pollAssert().run(() ->     assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));
    }
}
