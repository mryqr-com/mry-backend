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
import com.mryqr.support.PollingAssertion;
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
        String appId = response.appId();
        String groupName = rGroupName();

        CreateGroupCommand command = CreateGroupCommand.builder().name(groupName).appId(appId).build();
        String groupId = GroupApi.createGroup(response.jwt(), command);

        Group group = groupRepository.byId(groupId);
        assertEquals(groupId, group.getId());
        assertEquals(appId, group.getAppId());
        assertEquals(groupName, group.getName());
    }

    @Test
    public void app_manager_can_create_group() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        CreateMemberResponse member = MemberApi.createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());
        AppApi.setAppManager(response.jwt(), response.appId(), member.getMemberId());

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).build();
        String groupId = GroupApi.createGroup(member.getJwt(), command);

        Group group = groupRepository.byId(groupId);
        assertEquals(groupId, group.getId());
    }

    @Test
    public void should_raise_event_when_create_group() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.appId();

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).build();
        String groupId = GroupApi.createGroup(response.jwt(), command);

        GroupCreatedEvent groupCreatedEvent = latestEventFor(groupId, GROUP_CREATED, GroupCreatedEvent.class);
        assertEquals(groupId, groupCreatedEvent.getGroupId());
        assertEquals(appId, groupCreatedEvent.getAppId());
        Tenant tenant = tenantRepository.byId(response.tenantId());
        assertEquals(2, tenant.getResourceUsage().getGroupCountForApp(appId));
    }

    @Test
    public void create_group_should_also_update_group_hierarchy() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.appId();

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).build();
        String groupId = GroupApi.createGroup(response.jwt(), command);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(response.appId());
        assertEquals(2, groupHierarchy.groupCount());
        assertEquals(groupHierarchy.getHierarchy().schemaOf(groupId), groupId);
        assertEquals(groupHierarchy.getHierarchy().schemaOf(response.defaultGroupId()), response.defaultGroupId());
    }

    @Test
    public void should_fail_create_group_if_group_sync_enabled() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.appId();
        AppApi.enableGroupSync(response.jwt(), appId);

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).build();
        assertError(() -> GroupApi.createGroupRaw(response.jwt(), command), GROUP_SYNCED);
    }

    @Test
    public void should_fail_create_group_if_parent_group_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.appId();

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).parentGroupId(Group.newGroupId()).build();
        assertError(() -> GroupApi.createGroupRaw(response.jwt(), command), AR_NOT_FOUND);
    }

    @Test
    public void should_fail_create_group_if_hierarchy_too_deep() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupId1 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(response.defaultGroupId()).build());
        String groupId2 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId1).build());
        String groupId3 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId2).build());
        String groupId4 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId3).build());
        assertError(() -> GroupApi.createGroupRaw(response.jwt(),
                        CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId4).build()),
                GROUP_HIERARCHY_TOO_DEEP);
    }

    @Test
    public void should_fail_create_group_if_parent_not_visible() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String parentGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        GroupApi.deactivateGroup(response.jwt(), parentGroupId);

        assertError(() -> GroupApi.createGroupRaw(response.jwt(),
                        CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(parentGroupId).build()),
                GROUP_NOT_VISIBLE);
    }

    @Test
    public void common_member_should_fail_create_group() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        CreateMemberResponse member = MemberApi.createMemberAndLogin(response.jwt(), rMemberName(), rMobile(), rPassword());

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).build();

        assertError(() -> GroupApi.createGroupRaw(member.getJwt(), command), ACCESS_DENIED);
    }

    @Test
    public void should_fail_create_group_if_name_already_exits() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.appId();
        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(appId).build();
        GroupApi.createGroup(response.jwt(), command);

        assertError(() -> GroupApi.createGroupRaw(response.jwt(), command), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_create_group_with_same_name_in_different_level() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());

        String appId = response.appId();
        String groupName = rGroupName();
        String groupId1 = GroupApi.createGroup(response.jwt(), CreateGroupCommand.builder().name(groupName).appId(appId).build());
        String groupId2 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().parentGroupId(groupId1).name(groupName).appId(appId).build());
        Group group = groupRepository.byId(groupId2);
        assertEquals(groupName, group.getName());
    }

    @Test
    public void should_fail_create_group_if_name_already_exists() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String groupId = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().parentGroupId(response.defaultGroupId()).name(rGroupName()).appId(response.appId()).build());

        String groupName = rGroupName();
        CreateGroupCommand command = CreateGroupCommand.builder().parentGroupId(groupId).name(groupName).appId(response.appId()).build();
        GroupApi.createGroup(response.jwt(), command);
        assertError(() -> GroupApi.createGroupRaw(response.jwt(), command), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_create_group_if_packages_limit_reached() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        Tenant tenant = tenantRepository.byId(response.tenantId());
        int limit = tenant.getPackages().effectiveMaxGroupCountPerApp();
        tenant.setGroupCountForApp(response.appId(), limit);
        tenantRepository.save(tenant);

        CreateGroupCommand command = CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).build();
        assertError(() -> GroupApi.createGroupRaw(response.jwt(), command), GROUP_COUNT_LIMIT_REACHED);
    }

    @Test
    public void should_rename_group() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String name = rGroupName();
        GroupApi.renameGroup(response.jwt(), response.defaultGroupId(), name);

        Group group = groupRepository.byId(response.defaultGroupId());
        assertEquals(name, group.getName());
    }

    @Test
    public void should_fail_rename_group_if_name_already_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupName = rGroupName();
        GroupApi.createGroup(response.jwt(), response.appId(), groupName);

        RenameGroupCommand command = RenameGroupCommand.builder().name(groupName).build();
        assertError(() -> GroupApi.renameGroupRaw(response.jwt(), response.defaultGroupId(), command), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_fail_rename_group_if_name_already_exist_at_same_level() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupName = rGroupName();
        GroupApi.createGroup(response.jwt(), CreateGroupCommand.builder()
                .parentGroupId(response.defaultGroupId())
                .appId(response.appId())
                .name(groupName).build());

        String groupId = GroupApi.createGroup(response.jwt(), CreateGroupCommand.builder()
                .appId(response.appId())
                .parentGroupId(response.defaultGroupId())
                .name(rGroupName()).build());

        RenameGroupCommand command = RenameGroupCommand.builder().name(groupName).build();
        assertError(() -> GroupApi.renameGroupRaw(response.jwt(), groupId, command), GROUP_WITH_NAME_ALREADY_EXISTS);
    }

    @Test
    public void should_rename_group_to_same_name_but_different_level() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupName = rGroupName();
        GroupApi.createGroup(response.jwt(), CreateGroupCommand.builder()
                .parentGroupId(response.defaultGroupId())
                .appId(response.appId())
                .name(groupName).build());

        String groupId = GroupApi.createGroup(response.jwt(), CreateGroupCommand.builder()
                .appId(response.appId())
                .name(rGroupName()).build());

        RenameGroupCommand renameGroupCommand = RenameGroupCommand.builder().name(groupName).build();
        GroupApi.renameGroup(response.jwt(), groupId, renameGroupCommand);
        Group group = groupRepository.byId(groupId);
        assertEquals(groupName, group.getName());
    }

    @Test
    public void common_member_should_fail_rename_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        CreateMemberResponse memberResponse = MemberApi.createMemberAndLogin(response.jwt());

        RenameGroupCommand command = RenameGroupCommand.builder().name(rGroupName()).build();

        assertError(() -> GroupApi.renameGroupRaw(memberResponse.getJwt(), response.defaultGroupId(), command), ACCESS_DENIED);
    }

    @Test
    public void should_add_group_members() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.jwt());
        String memberId2 = MemberApi.createMember(response.jwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build();
        GroupApi.addGroupMembers(response.jwt(), response.defaultGroupId(), command);

        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.getMembers().containsAll(List.of(memberId1, memberId2)));
    }

    @Test
    public void should_fail_add_group_members_if_not_all_members_exists() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.jwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, Member.newMemberId())).build();
        assertError(() -> GroupApi.addGroupMembersRaw(response.jwt(), response.defaultGroupId(), command), NOT_ALL_MEMBERS_EXIST);
    }

    @Test
    public void should_remove_members_from_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.jwt());
        String memberId2 = MemberApi.createMember(response.jwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build();
        GroupApi.addGroupMembers(response.jwt(), response.defaultGroupId(), command);
        assertTrue(groupRepository.byId(response.defaultGroupId()).getMembers().containsAll(List.of(memberId1, memberId2)));

        GroupApi.removeGroupMember(response.jwt(), response.defaultGroupId(), memberId1);
        Group updatedGroup = groupRepository.byId(response.defaultGroupId());
        assertTrue(updatedGroup.getMembers().contains(memberId2));
        assertFalse(updatedGroup.getMembers().contains(memberId1));
    }

    @Test
    public void remove_members_should_also_remove_managers() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.jwt());
        String memberId2 = MemberApi.createMember(response.jwt());

        GroupApi.addGroupMembers(response.jwt(), response.defaultGroupId(),
                AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build());
        GroupApi.addGroupManager(response.jwt(), response.defaultGroupId(), memberId1);
        GroupManagersChangedEvent event = latestEventFor(response.defaultGroupId(), GROUP_MANAGERS_CHANGED, GroupManagersChangedEvent.class);
        assertEquals(response.defaultGroupId(), event.getGroupId());

        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.getMembers().containsAll(List.of(memberId1, memberId2)));
        assertTrue(group.getManagers().contains(memberId1));

        GroupApi.removeGroupMember(response.jwt(), response.defaultGroupId(), memberId1);
        Group updatedGroup = groupRepository.byId(response.defaultGroupId());
        assertTrue(updatedGroup.getMembers().contains(memberId2));
        assertFalse(updatedGroup.getMembers().contains(memberId1));
        assertFalse(updatedGroup.getManagers().contains(memberId1));
        GroupManagersChangedEvent anotherEvent = latestEventFor(response.defaultGroupId(), GROUP_MANAGERS_CHANGED,
                GroupManagersChangedEvent.class);
        assertEquals(response.defaultGroupId(), anotherEvent.getGroupId());
        assertNotEquals(event.getId(), anotherEvent.getId());
    }

    @Test
    public void should_add_group_manager() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.jwt());
        String memberId2 = MemberApi.createMember(response.jwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build();
        GroupApi.addGroupMembers(response.jwt(), response.defaultGroupId(), command);
        assertTrue(groupRepository.byId(response.defaultGroupId()).getManagers().isEmpty());

        GroupApi.addGroupManager(response.jwt(), response.defaultGroupId(), memberId1);
        assertTrue(groupRepository.byId(response.defaultGroupId()).getManagers().contains(memberId1));

        GroupManagersChangedEvent changedEvent = latestEventFor(response.defaultGroupId(), GROUP_MANAGERS_CHANGED,
                GroupManagersChangedEvent.class);
        assertEquals(response.defaultGroupId(), changedEvent.getGroupId());
    }

    @Test
    public void add_group_manager_should_also_add_as_member() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.jwt());

        GroupApi.addGroupManager(response.jwt(), response.defaultGroupId(), memberId);
        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.getMembers().contains(memberId));
        assertTrue(group.getManagers().contains(memberId));
    }

    @Test
    public void should_add_group_managers() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.jwt());

        GroupApi.addGroupManagers(response.jwt(), response.defaultGroupId(),
                AddGroupManagersCommand.builder().memberIds(List.of(memberId)).build());
        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.getMembers().contains(memberId));
        assertTrue(group.getManagers().contains(memberId));

        GroupManagersChangedEvent changedEvent = latestEventFor(response.defaultGroupId(), GROUP_MANAGERS_CHANGED,
                GroupManagersChangedEvent.class);
        assertEquals(response.defaultGroupId(), changedEvent.getGroupId());
    }

    @Test
    public void should_remove_group_manager() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId1 = MemberApi.createMember(response.jwt());
        String memberId2 = MemberApi.createMember(response.jwt());

        AddGroupMembersCommand command = AddGroupMembersCommand.builder().memberIds(List.of(memberId1, memberId2)).build();
        GroupApi.addGroupMembers(response.jwt(), response.defaultGroupId(), command);
        assertTrue(groupRepository.byId(response.defaultGroupId()).getManagers().isEmpty());

        GroupApi.addGroupManager(response.jwt(), response.defaultGroupId(), memberId1);
        assertTrue(groupRepository.byId(response.defaultGroupId()).getManagers().contains(memberId1));

        GroupApi.removeGroupManager(response.jwt(), response.defaultGroupId(), memberId1);
        assertTrue(groupRepository.byId(response.defaultGroupId()).getManagers().isEmpty());

        GroupManagersChangedEvent anotherEvent = latestEventFor(response.defaultGroupId(), GROUP_MANAGERS_CHANGED,
                GroupManagersChangedEvent.class);
        assertEquals(response.defaultGroupId(), anotherEvent.getGroupId());
    }

    @Test
    public void should_raise_event_when_group_managers_changed() {
        PreparedQrResponse response = setupApi.registerWithQr();
        String oldManagerMemberId = MemberApi.createMember(response.jwt());
        String newManagerMemberId = MemberApi.createMember(response.jwt());
        GroupApi.addGroupMembers(response.jwt(), response.defaultGroupId(),
                AddGroupMembersCommand.builder().memberIds(List.of(oldManagerMemberId, newManagerMemberId)).build());
        GroupApi.addGroupManager(response.jwt(), response.defaultGroupId(), oldManagerMemberId);

        Attribute groupManagerAttribute = Attribute.builder().id(newAttributeId()).name(rAttributeName()).type(INSTANCE_GROUP_MANAGERS).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), groupManagerAttribute);
        QR qr = qrRepository.byId(response.qrId());
        MembersAttributeValue membersAttributeValue = (MembersAttributeValue) qr.attributeValueOf(groupManagerAttribute.getId());
        assertTrue(membersAttributeValue.getMemberIds().contains(oldManagerMemberId));
        assertFalse(membersAttributeValue.getMemberIds().contains(newManagerMemberId));

        GroupApi.removeGroupManager(response.jwt(), response.defaultGroupId(), oldManagerMemberId);
        GroupApi.addGroupManager(response.jwt(), response.defaultGroupId(), newManagerMemberId);

        GroupManagersChangedEvent event = latestEventFor(response.defaultGroupId(), GROUP_MANAGERS_CHANGED, GroupManagersChangedEvent.class);
        assertEquals(response.appId(), event.getAppId());
        assertEquals(response.defaultGroupId(), event.getGroupId());
        QR updatedQr = qrRepository.byId(response.qrId());
        MembersAttributeValue updatedMembersAttributeValue = (MembersAttributeValue) updatedQr.attributeValueOf(groupManagerAttribute.getId());
        assertFalse(updatedMembersAttributeValue.getMemberIds().contains(oldManagerMemberId));
        assertTrue(updatedMembersAttributeValue.getMemberIds().contains(newManagerMemberId));
    }

    @Test
    public void should_delete_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupId = GroupApi.createGroup(response.jwt(), response.appId(), rGroupName());

        GroupApi.deleteGroup(response.jwt(), groupId);

        assertFalse(groupRepository.byIdOptional(groupId).isPresent());
    }

    @Test
    public void delete_group_should_also_delete_it_from_group_hierarchy() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String groupId = GroupApi.createGroup(response.jwt(), response.appId(), rGroupName());
        assertTrue(groupHierarchyRepository.byAppId(response.appId()).containsGroupId(groupId));

        GroupApi.deleteGroup(response.jwt(), groupId);
        assertFalse(groupHierarchyRepository.byAppId(response.appId()).containsGroupId(groupId));
    }

    @Test
    public void delete_group_should_also_delete_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupId = GroupApi.createGroup(response.jwt(), CreateGroupCommand.builder()
                .parentGroupId(response.defaultGroupId())
                .appId(response.appId())
                .name(rGroupName()).build());

        String groupId2 = GroupApi.createGroup(response.jwt(), CreateGroupCommand.builder()
                .parentGroupId(groupId)
                .appId(response.appId())
                .name(rGroupName()).build());

        GroupApi.deleteGroup(response.jwt(), groupId);
        assertFalse(groupRepository.exists(groupId2));
        IdTreeHierarchy hierarchy = groupHierarchyRepository.byAppId(response.appId()).getHierarchy();
        assertFalse(hierarchy.allIds().contains(groupId));
        assertFalse(hierarchy.allIds().contains(groupId2));

        assertEquals(groupId, latestEventFor(groupId, GROUP_DELETED, GroupDeletedEvent.class).getGroupId());
        assertEquals(groupId2, latestEventFor(groupId2, GROUP_DELETED, GroupDeletedEvent.class).getGroupId());
    }

    @Test
    public void should_archive_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroup(response.jwt(), response.appId());
        GroupApi.archiveGroup(response.jwt(), response.defaultGroupId());

        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.isArchived());
    }

    @Test
    public void should_not_archive_if_only_one_visible_group_left() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        GroupApi.archiveGroup(response.jwt(), response.defaultGroupId());

        assertError(() -> GroupApi.archiveGroupRaw(response.jwt(), anotherGroupId), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_not_archive_if_only_one_visible_group_left_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroupWithParent(response.jwt(), response.appId(), response.defaultGroupId());

        assertError(() -> GroupApi.archiveGroupRaw(response.jwt(), response.defaultGroupId()), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_un_archive_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroup(response.jwt(), response.appId());
        GroupApi.archiveGroup(response.jwt(), response.defaultGroupId());
        Group group = groupRepository.byId(response.defaultGroupId());
        assertTrue(group.isArchived());

        GroupApi.unArchiveGroup(response.jwt(), response.defaultGroupId());
        Group unarchivedGroup = groupRepository.byId(response.defaultGroupId());
        assertFalse(unarchivedGroup.isArchived());
    }

    @Test
    public void archive_and_un_archive_group_should_also_do_it_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupId1 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(response.defaultGroupId()).build());
        String groupId2 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId1).build());
        String groupId3 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId2).build());

        GroupApi.archiveGroup(response.jwt(), groupId1);
        assertTrue(groupRepository.byId(groupId1).isArchived());
        assertTrue(groupRepository.byId(groupId2).isArchived());
        assertTrue(groupRepository.byId(groupId3).isArchived());

        GroupApi.unArchiveGroup(response.jwt(), groupId1);
        assertFalse(groupRepository.byId(groupId1).isArchived());
        assertFalse(groupRepository.byId(groupId2).isArchived());
        assertFalse(groupRepository.byId(groupId3).isArchived());
    }

    @Test
    public void should_deactivate_and_activate_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroup(response.jwt(), response.appId());
        assertTrue(groupRepository.byId(response.defaultGroupId()).isActive());

        GroupApi.deactivateGroup(response.jwt(), response.defaultGroupId());
        assertFalse(groupRepository.byId(response.defaultGroupId()).isActive());

        GroupApi.activateGroup(response.jwt(), response.defaultGroupId());
        assertTrue(groupRepository.byId(response.defaultGroupId()).isActive());
    }

    @Test
    public void deactivate_and_activate_group_should_also_do_it_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String groupId1 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(response.defaultGroupId()).build());
        String groupId2 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId1).build());
        String groupId3 = GroupApi.createGroup(response.jwt(),
                CreateGroupCommand.builder().name(rGroupName()).appId(response.appId()).parentGroupId(groupId2).build());

        GroupApi.deactivateGroup(response.jwt(), groupId1);
        assertFalse(groupRepository.byId(groupId1).isActive());
        assertFalse(groupRepository.byId(groupId2).isActive());
        assertFalse(groupRepository.byId(groupId3).isActive());

        GroupApi.activateGroup(response.jwt(), groupId1);
        assertTrue(groupRepository.byId(groupId1).isActive());
        assertTrue(groupRepository.byId(groupId2).isActive());
        assertTrue(groupRepository.byId(groupId3).isActive());
    }

    @Test
    public void deactivate_group_should_sync_to_qrs_under_it() {
        PreparedQrResponse response = setupApi.registerWithQr();
        GroupApi.createGroup(response.jwt(), response.appId());
        assertTrue(qrRepository.byId(response.qrId()).isGroupActive());

        String subGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), response.defaultGroupId());
        CreateQrResponse subQrResponse = QrApi.createQr(response.jwt(), subGroupId);

        GroupApi.deactivateGroup(response.jwt(), response.defaultGroupId());
        assertEquals(response.defaultGroupId(),
                latestEventFor(response.defaultGroupId(), GROUP_DEACTIVATED, GroupDeactivatedEvent.class).getGroupId());
        assertEquals(subGroupId, latestEventFor(subGroupId, GROUP_DEACTIVATED, GroupDeactivatedEvent.class).getGroupId());
        assertFalse(qrRepository.byId(response.qrId()).isGroupActive());
        assertFalse(qrRepository.byId(subQrResponse.getQrId()).isGroupActive());

        GroupApi.activateGroup(response.jwt(), response.defaultGroupId());
        GroupActivatedEvent groupActivatedEvent = latestEventFor(response.defaultGroupId(), GROUP_ACTIVATED, GroupActivatedEvent.class);
        assertEquals(response.defaultGroupId(), groupActivatedEvent.getGroupId());
        assertTrue(qrRepository.byId(response.qrId()).isGroupActive());
        assertTrue(qrRepository.byId(subQrResponse.getQrId()).isGroupActive());
    }

    @Test
    public void should_not_deactivate_if_only_one_active_group_left() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        GroupApi.deactivateGroup(response.jwt(), response.defaultGroupId());

        assertError(() -> GroupApi.deactivateGroupRaw(response.jwt(), anotherGroupId), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_not_deactivate_if_only_one_visible_group_left_for_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        GroupApi.createGroupWithParent(response.jwt(), response.appId(), response.defaultGroupId());

        assertError(() -> GroupApi.deactivateGroupRaw(response.jwt(), response.defaultGroupId()), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_raise_event_when_delete_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        FSingleLineTextControl control = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        SingleLineTextAnswer answer = rAnswer(control);
        String plateBatchId = PlateBatchApi.createPlateBatch(response.jwt(), response.appId(), 10);
        List<String> plateIds = plateRepository.allPlateIdsUnderPlateBatch(plateBatchId);
        String plateId = plateIds.get(0);
        String groupId = GroupApi.createGroup(response.jwt(), response.appId(), rGroupName());
        CreateQrResponse qrResponse = QrApi.createQr(response.jwt(), groupId);
        QrApi.resetPlate(response.jwt(), qrResponse.getQrId(), plateId);
        String submissionId = SubmissionApi.newSubmission(response.jwt(), qrResponse.getQrId(), response.homePageId(), answer);
        assertTrue(qrRepository.byIdOptional(qrResponse.getQrId()).isPresent());
        assertTrue(submissionRepository.byIdOptional(submissionId).isPresent());
        assertTrue(plateRepository.byId(plateId).isBound());
        Tenant tenant = tenantRepository.byId(response.tenantId());
        assertEquals(2, tenant.getResourceUsage().getGroupCountForApp(response.appId()));
        assertEquals(1, tenant.getResourceUsage().getSubmissionCountForApp(response.appId()));
        PlateBatch plateBatch = plateBatchRepository.byId(plateBatchId);
        assertEquals(9, plateBatch.getAvailableCount());

        GroupApi.deleteGroup(response.jwt(), groupId);

        GroupDeletedEvent event = latestEventFor(groupId, GROUP_DELETED, GroupDeletedEvent.class);
        assertEquals(groupId, event.getGroupId());
        assertEquals(response.appId(), event.getAppId());
        assertFalse(qrRepository.byIdOptional(qrResponse.getQrId()).isPresent());
        assertFalse(submissionRepository.byIdOptional(submissionId).isPresent());
        assertFalse(plateRepository.byId(plateId).isBound());
        Tenant updatedTenant = tenantRepository.byId(response.tenantId());
        assertEquals(1, updatedTenant.getResourceUsage().getGroupCountForApp(response.appId()));
        assertEquals(0, updatedTenant.getResourceUsage().getSubmissionCountForApp(response.appId()));
        PlateBatch updatedBatch = plateBatchRepository.byId(plateBatchId);
        assertEquals(10, updatedBatch.getAvailableCount());
    }

    @Test
    public void should_fail_delete_group_if_only_one_visible_group_left() {
        PreparedAppResponse response = setupApi.registerWithApp();
        assertError(() -> GroupApi.deleteGroupRaw(response.jwt(), response.defaultGroupId()), NO_MORE_THAN_ONE_VISIBLE_GROUP_LEFT);
    }

    @Test
    public void should_fetch_group_members() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String memberId = MemberApi.createMember(response.jwt());

        GroupApi.addGroupMembers(response.jwt(), response.defaultGroupId(), response.memberId());
        GroupApi.addGroupManagers(response.jwt(), response.defaultGroupId(), memberId);

        QGroupMembers groupMembers = GroupApi.allGroupMembers(response.jwt(), response.defaultGroupId());
        assertEquals(2, groupMembers.getMemberIds().size());
        assertTrue(groupMembers.getMemberIds().contains(response.memberId()));
        assertTrue(groupMembers.getMemberIds().contains(memberId));
        assertEquals(1, groupMembers.getManagerIds().size());
        assertTrue(groupMembers.getManagerIds().contains(memberId));
    }

    @Test
    public void should_cache_group() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String key = "Cache:GROUP::" + response.defaultGroupId();
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));

        groupRepository.cachedById(response.defaultGroupId());
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(key)));

        Group group = groupRepository.byId(response.defaultGroupId());
        groupRepository.save(group);
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));
    }

    @Test
    public void should_cache_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String key = "Cache:APP_GROUPS::" + response.appId();
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));

        groupRepository.cachedAllGroupFullNames(response.appId());
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(key)));

        Group group = groupRepository.byId(response.defaultGroupId());
        groupRepository.save(group);
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(key)));
    }

    @Test
    public void save_group_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        groupRepository.cachedById(response.defaultGroupId());
        groupRepository.cachedById(anotherGroupId);
        groupRepository.cachedAppAllGroups(response.appId());

        String groupsKey = "Cache:APP_GROUPS::" + response.appId();
        String groupKey = "Cache:GROUP::" + response.defaultGroupId();
        String anotherGroupKey = "Cache:GROUP::" + anotherGroupId;

        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(groupsKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(groupKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey)));

        Group group = groupRepository.byId(response.defaultGroupId());
        groupRepository.save(group);

        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupsKey)));
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey)));
    }

    @Test
    public void save_groups_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        String yetAnotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        groupRepository.cachedById(response.defaultGroupId());
        groupRepository.cachedById(anotherGroupId);
        groupRepository.cachedById(yetAnotherGroupId);
        groupRepository.cachedAppAllGroups(response.appId());

        String groupsKey = "Cache:APP_GROUPS::" + response.appId();
        String groupKey = "Cache:GROUP::" + response.defaultGroupId();
        String anotherGroupKey = "Cache:GROUP::" + anotherGroupId;
        String yetAnotherGroupKey = "Cache:GROUP::" + yetAnotherGroupId;

        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(groupsKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(groupKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(yetAnotherGroupKey)));

        Group group = groupRepository.byId(response.defaultGroupId());
        Group yetAnotherGroup = groupRepository.byId(yetAnotherGroupId);
        groupRepository.save(List.of(group, yetAnotherGroup));

        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupsKey)));
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupKey)));
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(yetAnotherGroupKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey)));
    }

    @Test
    public void delete_group_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        groupRepository.cachedById(response.defaultGroupId());
        groupRepository.cachedById(anotherGroupId);
        groupRepository.cachedAppAllGroups(response.appId());

        String groupsKey = "Cache:APP_GROUPS::" + response.appId();
        String defaultGroupKey = "Cache:GROUP::" + response.defaultGroupId();
        String anotherGroupKey = "Cache:GROUP::" + anotherGroupId;

        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(groupsKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(defaultGroupKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey)));

        Group group = groupRepository.byId(response.defaultGroupId());
        group.onDelete(User.NO_USER);
        groupRepository.delete(group);

        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupsKey)));
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(defaultGroupKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey)));
    }

    @Test
    public void delete_groups_should_evict_cache() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        groupRepository.cachedById(response.defaultGroupId());
        groupRepository.cachedById(anotherGroupId);
        groupRepository.cachedAppAllGroups(response.appId());

        String groupsKey = "Cache:APP_GROUPS::" + response.appId();
        String defaultGroupKey = "Cache:GROUP::" + response.defaultGroupId();
        String anotherGroupKey = "Cache:GROUP::" + anotherGroupId;

        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(groupsKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(defaultGroupKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey)));

        Group group = groupRepository.byId(response.defaultGroupId());
        group.onDelete(User.NO_USER);
        groupRepository.delete(List.of(group));

        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(groupsKey)));
        PollingAssertion.pollAssert().run(() -> assertNotEquals(TRUE, stringRedisTemplate.hasKey(defaultGroupKey)));
        PollingAssertion.pollAssert().run(() -> assertEquals(TRUE, stringRedisTemplate.hasKey(anotherGroupKey)));
    }

    @Test
    public void should_list_group_qrs() {
        PreparedAppResponse response = setupApi.registerWithApp();

        CreateQrResponse qrResponse1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.jwt(), "3号机床", response.defaultGroupId());
        String anotherGroupId = GroupApi.createGroup(response.jwt(), response.appId());
        QrApi.createQr(response.jwt(), anotherGroupId);

        ListGroupQrsQuery simpleCommand = ListGroupQrsQuery.builder().pageIndex(1).pageSize(20).build();
        PagedList<QGroupQr> qrs = GroupApi.listGroupQrs(response.jwt(), response.defaultGroupId(), simpleCommand);
        assertEquals(2, qrs.getData().size());
        assertEquals(2, qrs.getTotalNumber());
        List<String> qrIds = qrs.getData().stream().map(QGroupQr::getId).toList();
        assertTrue(qrIds.contains(qrResponse1.getQrId()));
        assertTrue(qrIds.contains(qrResponse2.getQrId()));
        assertEquals(qrs.getData().get(0).getId(), qrResponse2.getQrId());

        ListGroupQrsQuery sortCommand = ListGroupQrsQuery.builder().sortedBy("createdAt").ascSort(true).pageIndex(1).pageSize(20).build();
        PagedList<QGroupQr> sortedQrs = GroupApi.listGroupQrs(response.jwt(), response.defaultGroupId(), sortCommand);
        assertEquals(2, sortedQrs.getData().size());
        assertEquals(sortedQrs.getData().get(0).getId(), qrResponse1.getQrId());

        ListGroupQrsQuery searchCommand = ListGroupQrsQuery.builder().search("机床").pageIndex(1).pageSize(20).build();
        PagedList<QGroupQr> searchedQrs = GroupApi.listGroupQrs(response.jwt(), response.defaultGroupId(), searchCommand);
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