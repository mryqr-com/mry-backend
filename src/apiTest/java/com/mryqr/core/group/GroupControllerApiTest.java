package com.mryqr.core.group;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.idnode.IdTreeHierarchy;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.utils.PagedList;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.group.command.AddGroupManagersCommand;
import com.mryqr.core.group.command.AddGroupMembersCommand;
import com.mryqr.core.group.command.CreateGroupCommand;
import com.mryqr.core.group.command.RenameGroupCommand;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.event.*;
import com.mryqr.core.group.query.ListGroupQrsQuery;
import com.mryqr.core.group.query.QGroupMembers;
import com.mryqr.core.group.query.QGroupQr;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.member.MemberApi;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.platebatch.PlateBatchApi;
import com.mryqr.core.platebatch.domain.PlateBatch;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.MembersAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.CreateMemberResponse;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.mryqr.common.event.DomainEventType.*;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GROUP_MANAGERS;
import static com.mryqr.utils.RandomTestFixture.*;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

class GroupControllerApiTest extends BaseApiTest {

    @Test
    public void tenant_admin_can_create_group() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        String groupName = rGroupName();

        CreateGroupCommand command = CreateGroupCommand.builder().name(groupName).appId(appId).build();
        String groupId = GroupApi.createGroup(response.getJwt(), command);

        Group group = groupRepository.byId(groupId);
        assertEquals(groupId, group.getId());
        assertEquals(appId, group.getAppId());
        assertEquals(groupName, group.getName());
    }

    @Test
    public void app_manager_can_create_group() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        CreateMemberResponse member = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());
        AppApi.setAppManager(response.getJwt(), response.getAppId(), member.getMemberId());

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).build();
        String groupId = GroupApi.createGroup(member.getJwt(), command);

        Group group = groupRepository.byId(groupId);
        assertEquals(groupId, group.getId());
    }

    @Test
    public void should_raise_event_when_create_group() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).build();
        String groupId = GroupApi.createGroup(response.getJwt(), command);

        GroupCreatedEvent groupCreatedEvent = latestEventFor(groupId, GROUP_CREATED, GroupCreatedEvent.class);
        assertEquals(groupId, groupCreatedEvent.getGroupId());
        assertEquals(appId, groupCreatedEvent.getAppId());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(2, tenant.getResourceUsage().getGroupCountForApp(appId));
    }

    @Test
    public void create_group_should_also_update_group_hierarchy() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).build();
        String groupId = GroupApi.createGroup(response.getJwt(), command);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(response.getAppId());
        assertEquals(2, groupHierarchy.groupCount());
        assertEquals(groupHierarchy.getHierarchy().schemaOf(groupId), groupId);
        assertEquals(groupHierarchy.getHierarchy().schemaOf(response.getDefaultGroupId()), response.getDefaultGroupId());
    }

    @Test
    public void should_fail_create_group_if_group_sync_enabled() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();
        AppApi.enableGroupSync(response.getJwt(), appId);

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).build();
        assertError(() -> GroupApi.createGroupRaw(response.getJwt(), command), GROUP_SYNCED);
    }

    @Test
    public void should_fail_create_group_if_parent_group_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).parentGroupId(Group.newGroupId()).build();
        assertError(() -> GroupApi.createGroupRaw(response.getJwt(), command), AR_NOT_FOUND);
    }

    @Test
    public void should_fail_create_group_if_hierarchy_too_deep() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupId1 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(response.getDefaultGroupId()).build());
        String groupId2 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(groupId1).build());
        String groupId3 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(groupId2).build());
        String groupId4 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(groupId3).build());
        assertError(() -> GroupApi.createGroupRaw(response.getJwt(),
                        CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(groupId4).build()),
                GROUP_HIERARCHY_TOO_DEEP);
    }

    @Test
    public void should_fail_create_group_if_parent_not_visible() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String parentGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        GroupApi.deactivateGroup(response.getJwt(), parentGroupId);

        assertError(() -> GroupApi.createGroupRaw(response.getJwt(),
                        CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(parentGroupId).build()),
                GROUP_NOT_VISIBLE);
    }

    @Test
    public void common_member_should_fail_create_group() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        CreateMemberResponse member = MemberApi.createMemberAndLogin(response.getJwt(), rMemberName(), rMobile(), rPassword());

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).build();

        assertError(() -> GroupApi.createGroupRaw(member.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void should_fail_create_group_if_name_already_exits() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).build();
        GroupApi.createGroup(response.getJwt(), command);

        assertError(() -> GroupApi.createGroupRaw(response.getJwt(), command), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_create_group_with_same_name_in_different_level() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());

        String appId = response.getAppId();
        String groupName = rGroupName();
        String groupId1 = GroupApi.createGroup(response.getJwt(), CreateGroupCommand.builder().name(groupName).appId(appId).build());
        String groupId2 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().parentGroupId(groupId1).name(groupName).appId(appId).build());
        Group group = groupRepository.byId(groupId2);
        assertEquals(groupName, group.getName());
    }

    @Test
    public void should_fail_create_group_if_name_already_exists() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String groupId = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().parentGroupId(response.getDefaultGroupId()).name(rGroupName()).appId(response.getAppId()).build());

        String groupName = rGroupName();
        CreateGroupCommand command = CreateGroupCommand.builder().parentGroupId(groupId).name(groupName).appId(response.getAppId()).build();
        GroupApi.createGroup(response.getJwt(), command);
        assertError(() -> GroupApi.createGroupRaw(response.getJwt(), command), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_group_if_packages_limit_reached() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        int limit = tenant.getPackages().effectiveMaxGroupCountPerApp();
        tenant.setGroupCountForApp(response.getAppId(), limit);
        tenantRepository.save(tenant);

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).build();
        assertError(() -> GroupApi.createGroupRaw(response.getJwt(), command), GROUP_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_rename_group() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String name = rGroupName();
        GroupApi.renameGroup(response.getJwt(), response.getDefaultGroupId(), name);

        Group group = groupRepository.byId(response.getDefaultGroupId());
        assertEquals(name, group.getName());
    }

    @Test
    public void should_fail_rename_group_if_name_already_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupName = rGroupName();
        GroupApi.createGroup(response.getJwt(), response.getAppId(), groupName);

        RenameGroupCommand command = RenameGroupCommand.builder().name(groupName).build();
        assertError(() -> GroupApi.renameGroupRaw(response.getJwt(), response.getDefaultGroupId(), command), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_rename_group_if_name_already_exist_at_same_level() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupName = rGroupName();
        GroupApi.createGroup(response.getJwt(), CreateGroupCommand.builder()
                .parentGroupId(response.getDefaultGroupId())
                .appId(response.getAppId())
                .name(groupName).build());

        String groupId = GroupApi.createGroup(response.getJwt(), CreateGroupCommand.builder()
                .appId(response.getAppId())
                .parentGroupId(response.getDefaultGroupId())
                .name(rGroupName()).build());

        RenameGroupCommand command = RenameGroupCommand.builder().name(groupName).build();
        assertError(() -> GroupApi.renameGroupRaw(response.getJwt(), groupId, command), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_rename_group_to_same_name_but_different_level() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupName = rGroupName();
        GroupApi.createGroup(response.getJwt(), CreateGroupCommand.builder()
                .parentGroupId(response.getDefaultGroupId())
                .appId(response.getAppId())
                .name(groupName).build());

        String groupId = GroupApi.createGroup(response.getJwt(), CreateGroupCommand.builder()
                .appId(response.getAppId())
                .name(rGroupName()).build());

        RenameGroupCommand renameGroupCommand = RenameGroupCommand.builder().name(groupName).build();
        GroupApi.renameGroup(response.getJwt(), groupId, renameGroupCommand);
        Group group = groupRepository.byId(groupId);
        assertEquals(groupName, group.getName());
    }

    @Test
    public void common_member_should_fail_rename_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.getJwt());

        RenameGroupCommand command = RenameGroupCommand.builder().name(rGroupName()).build();

        assertError(() -> GroupApi.renameGroupRaw(memberResponse.getJwt(), response.getDefaultGroupId(), command), ACCESS_DENIED);
    }

    @Test
    public void should_add_group_members() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build();
        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(), command);

        Group group = groupRepository.byId(response.getDefaultGroupId());
        assertTrue(group.getMembers().containsAll(List.of(memberId1, memberId2)));
    }

    @Test
    public void should_fail_add_group_members_if_not_all_members_exists() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.getJwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, Member.newMemberId())).build();
        assertError(() -> GroupApi.addGroupMembersRaw(response.getJwt(), response.getDefaultGroupId(), command), NOT_ALL_MEMBERS_EXIST);
    }

    @Test
    public void should_remove_members_from_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build();
        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(), command);
        assertTrue(groupRepository.byId(response.getDefaultGroupId()).getMembers().containsAll(List.of(memberId1, memberId2)));

        GroupApi.removeGroupMember(response.getJwt(), response.getDefaultGroupId(), memberId1);
        Group updatedGroup = groupRepository.byId(response.getDefaultGroupId());
        assertTrue(updatedGroup.getMembers().contains(memberId2));
        assertFalse(updatedGroup.getMembers().contains(memberId1));
    }

    @Test
    public void remove_members_should_also_remove_managers() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());

        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(),
                AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build());
        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), memberId1);
        GroupManagersChangedEvent event = latestEventFor(response.getDefaultGroupId(), GROUP_MANAGERS_CHANGED, GroupManagersChangedEvent.class);
        assertEquals(response.getDefaultGroupId(), event.getGroupId());

        Group group = groupRepository.byId(response.getDefaultGroupId());
        assertTrue(group.getMembers().containsAll(List.of(memberId1, memberId2)));
        assertTrue(group.getManagers().contains(memberId1));

        GroupApi.removeGroupMember(response.getJwt(), response.getDefaultGroupId(), memberId1);
        Group updatedGroup = groupRepository.byId(response.getDefaultGroupId());
        assertTrue(updatedGroup.getMembers().contains(memberId2));
        assertFalse(updatedGroup.getMembers().contains(memberId1));
        assertFalse(updatedGroup.getManagers().contains(memberId1));
        GroupManagersChangedEvent anotherEvent = latestEventFor(response.getDefaultGroupId(), GROUP_MANAGERS_CHANGED,
                GroupManagersChangedEvent.class);
        assertEquals(response.getDefaultGroupId(), anotherEvent.getGroupId());
        assertNotEquals(event.getId(), anotherEvent.getId());
    }

    @Test
    public void should_add_group_manager() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build();
        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(), command);
        assertTrue(groupRepository.byId(response.getDefaultGroupId()).getManagers().isEmpty());

        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), memberId1);
        assertTrue(groupRepository.byId(response.getDefaultGroupId()).getManagers().contains(memberId1));

        GroupManagersChangedEvent changedEvent = latestEventFor(response.getDefaultGroupId(), GROUP_MANAGERS_CHANGED,
                GroupManagersChangedEvent.class);
        assertEquals(response.getDefaultGroupId(), changedEvent.getGroupId());
    }

    @Test
    public void add_group_manager_should_also_add_as_member() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.getJwt());

        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), memberId);
        Group group = groupRepository.byId(response.getDefaultGroupId());
        assertTrue(group.getMembers().contains(memberId));
        assertTrue(group.getManagers().contains(memberId));
    }

    @Test
    public void should_add_group_managers() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.getJwt());

        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(),
                AddGroupManagersCommand.builder().memberIds(List.of(memberId)).build());
        Group group = groupRepository.byId(response.getDefaultGroupId());
        assertTrue(group.getMembers().contains(memberId));
        assertTrue(group.getManagers().contains(memberId));

        GroupManagersChangedEvent changedEvent = latestEventFor(response.getDefaultGroupId(), GROUP_MANAGERS_CHANGED,
                GroupManagersChangedEvent.class);
        assertEquals(response.getDefaultGroupId(), changedEvent.getGroupId());
    }

    @Test
    public void should_remove_group_manager() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.getJwt());
        String memberId2 = MemberApi.createMember(response.getJwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build();
        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(), command);
        assertTrue(groupRepository.byId(response.getDefaultGroupId()).getManagers().isEmpty());

        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), memberId1);
        assertTrue(groupRepository.byId(response.getDefaultGroupId()).getManagers().contains(memberId1));

        GroupApi.removeGroupManager(response.getJwt(), response.getDefaultGroupId(), memberId1);
        assertTrue(groupRepository.byId(response.getDefaultGroupId()).getManagers().isEmpty());

        GroupManagersChangedEvent anotherEvent = latestEventFor(response.getDefaultGroupId(), GROUP_MANAGERS_CHANGED,
                GroupManagersChangedEvent.class);
        assertEquals(response.getDefaultGroupId(), anotherEvent.getGroupId());
    }

    @Test
    public void should_raise_event_when_group_managers_changed() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String oldManagerMemberId = MemberApi.createMember(response.getJwt());
        String newManagerMemberId = MemberApi.createMember(response.getJwt());
        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(),
                AddGroupMembersCommand.builder().memberIds(List.of(oldManagerMemberId, newManagerMemberId)).build());
        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), oldManagerMemberId);

        Attribute groupManagerAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP_MANAGERS).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), groupManagerAttribute);
        QR qr = qrRepository.byId(response.getQrId());
        MembersAttributeValue membersAttributeValue = (MembersAttributeValue) qr.attributeValueOf(groupManagerAttribute.getId());
        assertTrue(membersAttributeValue.getMemberIds().contains(oldManagerMemberId));
        assertFalse(membersAttributeValue.getMemberIds().contains(newManagerMemberId));

        GroupApi.removeGroupManager(response.getJwt(), response.getDefaultGroupId(), oldManagerMemberId);
        GroupApi.addGroupManager(response.getJwt(), response.getDefaultGroupId(), newManagerMemberId);

        GroupManagersChangedEvent event = latestEventFor(response.getDefaultGroupId(), GROUP_MANAGERS_CHANGED, GroupManagersChangedEvent.class);
        assertEquals(response.getAppId(), event.getAppId());
        assertEquals(response.getDefaultGroupId(), event.getGroupId());
        QR updatedQr = qrRepository.byId(response.getQrId());
        MembersAttributeValue updatedMembersAttributeValue = (MembersAttributeValue) updatedQr.attributeValueOf(groupManagerAttribute.getId());
        assertFalse(updatedMembersAttributeValue.getMemberIds().contains(oldManagerMemberId));
        assertTrue(updatedMembersAttributeValue.getMemberIds().contains(newManagerMemberId));
    }

    @Test
    public void should_delete_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupId = GroupApi.createGroup(response.getJwt(), response.getAppId(), rGroupName());

        GroupApi.deleteGroup(response.getJwt(), groupId);

        assertFalse(groupRepository.byIdOptional(groupId).isPresent());
    }

    @Test
    public void delete_group_should_also_delete_it_from_group_hierarchy() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupId = GroupApi.createGroup(response.getJwt(), response.getAppId(), rGroupName());
        assertTrue(groupHierarchyRepository.byAppId(response.getAppId()).containsGroupId(groupId));

        GroupApi.deleteGroup(response.getJwt(), groupId);
        assertFalse(groupHierarchyRepository.byAppId(response.getAppId()).containsGroupId(groupId));
    }

    @Test
    public void delete_group_should_also_delete_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupId = GroupApi.createGroup(response.getJwt(), CreateGroupCommand.builder()
                .parentGroupId(response.getDefaultGroupId())
                .appId(response.getAppId())
                .name(rGroupName()).build());

        String groupId2 = GroupApi.createGroup(response.getJwt(), CreateGroupCommand.builder()
                .parentGroupId(groupId)
                .appId(response.getAppId())
                .name(rGroupName()).build());

        GroupApi.deleteGroup(response.getJwt(), groupId);
        assertFalse(groupRepository.exists(groupId2));
        IdTreeHierarchy hierarchy = groupHierarchyRepository.byAppId(response.getAppId()).getHierarchy();
        assertFalse(hierarchy.allIds().contains(groupId));
        assertFalse(hierarchy.allIds().contains(groupId2));

        assertEquals(groupId, latestEventFor(groupId, GROUP_DELETED, GroupDeletedEvent.class).getGroupId());
        assertEquals(groupId2, latestEventFor(groupId2, GROUP_DELETED, GroupDeletedEvent.class).getGroupId());
    }

    @Test
    public void should_archive_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroup(response.getJwt(), response.getAppId());
        GroupApi.archiveGroup(response.getJwt(), response.getDefaultGroupId());

        Group group = groupRepository.byId(response.getDefaultGroupId());
        assertTrue(group.isArchived());
    }

    @Test
    public void should_not_archive_if_only_one_visible_group_left() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        GroupApi.archiveGroup(response.getJwt(), response.getDefaultGroupId());

        assertError(() -> GroupApi.archiveGroupRaw(response.getJwt(), anotherGroupId), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_not_archive_if_only_one_visible_group_left_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());

        assertError(() -> GroupApi.archiveGroupRaw(response.getJwt(), response.getDefaultGroupId()), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_un_archive_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroup(response.getJwt(), response.getAppId());
        GroupApi.archiveGroup(response.getJwt(), response.getDefaultGroupId());
        Group group = groupRepository.byId(response.getDefaultGroupId());
        assertTrue(group.isArchived());

        GroupApi.unArchiveGroup(response.getJwt(), response.getDefaultGroupId());
        Group unarchivedGroup = groupRepository.byId(response.getDefaultGroupId());
        assertFalse(unarchivedGroup.isArchived());
    }

    @Test
    public void archive_and_un_archive_group_should_also_do_it_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupId1 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(response.getDefaultGroupId()).build());
        String groupId2 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(groupId1).build());
        String groupId3 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(groupId2).build());

        GroupApi.archiveGroup(response.getJwt(), groupId1);
        assertTrue(groupRepository.byId(groupId1).isArchived());
        assertTrue(groupRepository.byId(groupId2).isArchived());
        assertTrue(groupRepository.byId(groupId3).isArchived());

        GroupApi.unArchiveGroup(response.getJwt(), groupId1);
        assertFalse(groupRepository.byId(groupId1).isArchived());
        assertFalse(groupRepository.byId(groupId2).isArchived());
        assertFalse(groupRepository.byId(groupId3).isArchived());
    }

    @Test
    public void should_deactivate_and_activate_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroup(response.getJwt(), response.getAppId());
        assertTrue(groupRepository.byId(response.getDefaultGroupId()).isActive());

        GroupApi.deactivateGroup(response.getJwt(), response.getDefaultGroupId());
        assertFalse(groupRepository.byId(response.getDefaultGroupId()).isActive());

        GroupApi.activateGroup(response.getJwt(), response.getDefaultGroupId());
        assertTrue(groupRepository.byId(response.getDefaultGroupId()).isActive());
    }

    @Test
    public void deactivate_and_activate_group_should_also_do_it_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupId1 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(response.getDefaultGroupId()).build());
        String groupId2 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(groupId1).build());
        String groupId3 = GroupApi.createGroup(response.getJwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.getAppId()).parentGroupId(groupId2).build());

        GroupApi.deactivateGroup(response.getJwt(), groupId1);
        assertFalse(groupRepository.byId(groupId1).isActive());
        assertFalse(groupRepository.byId(groupId2).isActive());
        assertFalse(groupRepository.byId(groupId3).isActive());

        GroupApi.activateGroup(response.getJwt(), groupId1);
        assertTrue(groupRepository.byId(groupId1).isActive());
        assertTrue(groupRepository.byId(groupId2).isActive());
        assertTrue(groupRepository.byId(groupId3).isActive());
    }

    @Test
    public void deactivate_group_should_sync_to_qrs_under_it() {
        PreparedQrResponse response = setupApi.registerWithQr();
        GroupApi.createGroup(response.getJwt(), response.getAppId());
        assertTrue(qrRepository.byId(response.getQrId()).isGroupActive());

        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());
        CreateQrResponse subQrResponse = QrApi.createQr(response.getJwt(), subGroupId);

        GroupApi.deactivateGroup(response.getJwt(), response.getDefaultGroupId());
        assertEquals(response.getDefaultGroupId(),
                latestEventFor(response.getDefaultGroupId(), GROUP_DEACTIVATED, GroupDeactivatedEvent.class).getGroupId());
        assertEquals(subGroupId, latestEventFor(subGroupId, GROUP_DEACTIVATED, GroupDeactivatedEvent.class).getGroupId());
        assertFalse(qrRepository.byId(response.getQrId()).isGroupActive());
        assertFalse(qrRepository.byId(subQrResponse.getQrId()).isGroupActive());

        GroupApi.activateGroup(response.getJwt(), response.getDefaultGroupId());
        GroupActivatedEvent groupActivatedEvent = latestEventFor(response.getDefaultGroupId(), GROUP_ACTIVATED, GroupActivatedEvent.class);
        assertEquals(response.getDefaultGroupId(), groupActivatedEvent.getGroupId());
        assertTrue(qrRepository.byId(response.getQrId()).isGroupActive());
        assertTrue(qrRepository.byId(subQrResponse.getQrId()).isGroupActive());
    }

    @Test
    public void should_not_deactivate_if_only_one_active_group_left() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        GroupApi.deactivateGroup(response.getJwt(), response.getDefaultGroupId());

        assertError(() -> GroupApi.deactivateGroupRaw(response.getJwt(), anotherGroupId), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_not_deactivate_if_only_one_visible_group_left_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());

        assertError(() -> GroupApi.deactivateGroupRaw(response.getJwt(), response.getDefaultGroupId()), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_raise_event_when_delete_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        SingleLineTextAnswer answer = rAnswer(control);
        String plateBatchId = PlateBatchApi.createPlateBatch(response.getJwt(), response.getAppId(), 10);
        List<String> plateIds = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId);
        String plateId = plateIds.get(0);
        String groupId = GroupApi.createGroup(response.getJwt(), response.getAppId(), rGroupName());
        CreateQrResponse qrResponse = QrApi.createQr(response.getJwt(), groupId);
        QrApi.resetPlate(response.getJwt(), qrResponse.getQrId(), plateId);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), qrResponse.getQrId(), response.getHomePageId(), answer);
        assertTrue(qrRepository.byIdOptional(qrResponse.getQrId()).isPresent());
        assertTrue(submissionRepository.byIdOptional(submissionId).isPresent());
        assertTrue(plateRepository.byId(plateId).isBound());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(2, tenant.getResourceUsage().getGroupCountForApp(response.getAppId()));
        assertEquals(1, tenant.getResourceUsage().getSubmissionCountForApp(response.getAppId()));
        PlateBatch plateBatch = plateBatchRepository.byId(plateBatchId);
        assertEquals(9, plateBatch.getAvailableCount());

        GroupApi.deleteGroup(response.getJwt(), groupId);

        GroupDeletedEvent event = latestEventFor(groupId, GROUP_DELETED, GroupDeletedEvent.class);
        assertEquals(groupId, event.getGroupId());
        assertEquals(response.getAppId(), event.getAppId());
        assertFalse(qrRepository.byIdOptional(qrResponse.getQrId()).isPresent());
        assertFalse(submissionRepository.byIdOptional(submissionId).isPresent());
        assertFalse(plateRepository.byId(plateId).isBound());
        Tenant updatedTenant = tenantRepository.byId(response.getTenantId());
        assertEquals(1, updatedTenant.getResourceUsage().getGroupCountForApp(response.getAppId()));
        assertEquals(0, updatedTenant.getResourceUsage().getSubmissionCountForApp(response.getAppId()));
        PlateBatch updatedBatch = plateBatchRepository.byId(plateBatchId);
        assertEquals(10, updatedBatch.getAvailableCount());
    }

    @Test
    public void should_fail_delete_group_if_only_one_visible_group_left() {
        PreparedAppResponse response = setupApi.registerWithApp();
        assertError(() -> GroupApi.deleteGroupRaw(response.getJwt(), response.getDefaultGroupId()), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_fetch_group_members() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.getJwt());

        GroupApi.addGroupMembers(response.getJwt(), response.getDefaultGroupId(), response.getMemberId());
        GroupApi.addGroupManagers(response.getJwt(), response.getDefaultGroupId(), memberId);

        QGroupMembers groupMembers = GroupApi.allGroupMembers(response.getJwt(), response.getDefaultGroupId());
        assertEquals(2, groupMembers.getMemberIds().size());
        assertTrue(groupMembers.getMemberIds().contains(response.getMemberId()));
        assertTrue(groupMembers.getMemberIds().contains(memberId));
        assertEquals(1, groupMembers.getManagerIds().size());
        assertTrue(groupMembers.getManagerIds().contains(memberId));
    }

    @Test
    public void should_cache_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String key = "Cache:GROUP::" + response.getDefaultGroupId();
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));

        groupRepository.cachedById(response.getDefaultGroupId());
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        Group group = groupRepository.byId(response.getDefaultGroupId());
        groupRepository.save(group);
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
    }

    @Test
    public void should_cache_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String key = "Cache:APP_GROUPS::" + response.getAppId();
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));

        groupRepository.cachedAllGroupFullNames(response.getAppId());
        assertEquals(TRUE, stringRedisTemplate.hasKey(key));

        Group group = groupRepository.byId(response.getDefaultGroupId());
        groupRepository.save(group);
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(key));
    }

    @Test
    public void save_group_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        groupRepository.cachedById(response.getDefaultGroupId());
        groupRepository.cachedById(anotherGroupId);
        groupRepository.cachedAppAllGroups(response.getAppId());

        String groupsKey = "Cache:APP_GROUPS::" + response.getAppId();
        String groupKey = "Cache:GROUP::" + response.getDefaultGroupId();
        String anotherGroupKey = "Cache:GROUP::" + anotherGroupId;

        assertEquals(TRUE, stringRedisTemplate.hasKey(groupsKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(groupKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey));

        Group group = groupRepository.byId(response.getDefaultGroupId());
        groupRepository.save(group);

        assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupsKey));
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey));
    }

    @Test
    public void save_groups_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        String yetAnotherGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        groupRepository.cachedById(response.getDefaultGroupId());
        groupRepository.cachedById(anotherGroupId);
        groupRepository.cachedById(yetAnotherGroupId);
        groupRepository.cachedAppAllGroups(response.getAppId());

        String groupsKey = "Cache:APP_GROUPS::" + response.getAppId();
        String groupKey = "Cache:GROUP::" + response.getDefaultGroupId();
        String anotherGroupKey = "Cache:GROUP::" + anotherGroupId;
        String yetAnotherGroupKey = "Cache:GROUP::" + yetAnotherGroupId;

        assertEquals(TRUE, stringRedisTemplate.hasKey(groupsKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(groupKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(yetAnotherGroupKey));

        Group group = groupRepository.byId(response.getDefaultGroupId());
        Group yetAnotherGroup = groupRepository.byId(yetAnotherGroupId);
        groupRepository.save(List.of(group, yetAnotherGroup));

        assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupsKey));
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupKey));
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(yetAnotherGroupKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey));
    }

    @Test
    public void delete_group_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        groupRepository.cachedById(response.getDefaultGroupId());
        groupRepository.cachedById(anotherGroupId);
        groupRepository.cachedAppAllGroups(response.getAppId());

        String groupsKey = "Cache:APP_GROUPS::" + response.getAppId();
        String defaultGroupKey = "Cache:GROUP::" + response.getDefaultGroupId();
        String anotherGroupKey = "Cache:GROUP::" + anotherGroupId;

        assertEquals(TRUE, stringRedisTemplate.hasKey(groupsKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(defaultGroupKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey));

        Group group = groupRepository.byId(response.getDefaultGroupId());
        group.onDelete(User.NOUSER);
        groupRepository.delete(group);

        assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupsKey));
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(defaultGroupKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey));
    }

    @Test
    public void delete_groups_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        groupRepository.cachedById(response.getDefaultGroupId());
        groupRepository.cachedById(anotherGroupId);
        groupRepository.cachedAppAllGroups(response.getAppId());

        String groupsKey = "Cache:APP_GROUPS::" + response.getAppId();
        String defaultGroupKey = "Cache:GROUP::" + response.getDefaultGroupId();
        String anotherGroupKey = "Cache:GROUP::" + anotherGroupId;

        assertEquals(TRUE, stringRedisTemplate.hasKey(groupsKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(defaultGroupKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey));

        Group group = groupRepository.byId(response.getDefaultGroupId());
        group.onDelete(User.NOUSER);
        groupRepository.delete(List.of(group));

        assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupsKey));
        assertNotEquals(TRUE, stringRedisTemplate.hasKey(defaultGroupKey));
        assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey));
    }

    @Test
    public void should_list_group_qrs() {
        PreparedAppResponse response = setupApi.registerWithApp();

        CreateQrResponse qrResponse1 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.getJwt(), "3号机床", response.getDefaultGroupId());
        String anotherGroupId = GroupApi.createGroup(response.getJwt(), response.getAppId());
        QrApi.createQr(response.getJwt(), anotherGroupId);

        ListGroupQrsQuery simpleCommand = ListGroupQrsQuery.builder().pageIndex(1).pageSize(20).build();
        PagedList<QGroupQr> qrs = GroupApi.listGroupQrs(response.getJwt(), response.getDefaultGroupId(), simpleCommand);
        assertEquals(2, qrs.getData().size());
        assertEquals(2, qrs.getTotalNumber());
        List<String> qrIds = qrs.getData().stream().map(QGroupQr::getId).toList();
        assertTrue(qrIds.contains(qrResponse1.getQrId()));
        assertTrue(qrIds.contains(qrResponse2.getQrId()));
        assertEquals(qrs.getData().get(0).getId(), qrResponse2.getQrId());

        ListGroupQrsQuery sortCommand = ListGroupQrsQuery.builder().sortedBy("createdAt").ascSort(true).pageIndex(1).pageSize(20).build();
        PagedList<QGroupQr> sortedQrs = GroupApi.listGroupQrs(response.getJwt(), response.getDefaultGroupId(), sortCommand);
        assertEquals(2, sortedQrs.getData().size());
        assertEquals(sortedQrs.getData().get(0).getId(), qrResponse1.getQrId());

        ListGroupQrsQuery searchCommand = ListGroupQrsQuery.builder().search("机床").pageIndex(1).pageSize(20).build();
        PagedList<QGroupQr> searchedQrs = GroupApi.listGroupQrs(response.getJwt(), response.getDefaultGroupId(), searchCommand);
        assertEquals(1, searchedQrs.getData().size());
        QGroupQr groupQr = searchedQrs.getData().get(0);
        assertEquals(groupQr.getId(), qrResponse2.getQrId());

        QR qr2 = qrRepository.byId(qrResponse2.getQrId());
        assertEquals(qr2.getPlateId(), groupQr.getPlateId());
        assertEquals(qr2.getName(), groupQr.getName());
        assertEquals(qr2.getGroupId(), groupQr.getGroupId());
        assertEquals(qr2.getCreatedAt(), groupQr.getCreatedAt());
    }
}