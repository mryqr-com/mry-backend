package com.mryqr.integration.group.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupDomainService;
import com.mryqr.core.group.domain.GroupFactory;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.NOT_ALL_MEMBERS_EXIST;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationGroupCommandService {
    private final AppRepository appRepository;
    private final MryRateLimiter mryRateLimiter;
    private final GroupRepository groupRepository;
    private final TenantRepository tenantRepository;
    private final GroupDomainService groupDomainService;
    private final GroupFactory groupFactory;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public String createGroup(IntegrationCreateGroupCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Create", 10);

        String appId = command.getAppId();
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(app.getTenantId());
        packagesStatus.validateAddGroup(appId);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(appId);
        Group group = groupFactory.create(command.getName(), command.getParentGroupId(), groupHierarchy, app, command.getCustomId(), user);
        groupHierarchy.addGroup(command.getParentGroupId(), group.getId(), user);

        groupRepository.save(group);
        groupHierarchyRepository.save(groupHierarchy);
        log.info("Integration created group[{}].", group.getId());
        return group.getId();
    }

    @Transactional
    public void deleteGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Delete", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        doDeleteGroup(group, user);
        log.info("Integration deleted group[{}].", groupId);
    }

    @Transactional
    public void deleteGroupByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:Delete", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        doDeleteGroup(group, user);
        log.info("Integration deleted group[appId={},customId={}].", appId, customId);
    }

    private void doDeleteGroup(Group group, User user) {
        App app = appRepository.cachedById(group.getAppId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(group.getId());
        groupDomainService.checkDeleteGroups(app, concat(allSubGroupIds.stream(), of(group.getId())).collect(toImmutableSet()));

        group.onDelete(user);
        groupRepository.delete(group);
        if (!allSubGroupIds.isEmpty()) {
            List<Group> allTobeDeletedSubGroups = groupRepository.byIds(allSubGroupIds);
            allTobeDeletedSubGroups.forEach(it -> it.onDelete(user));
            groupRepository.delete(allTobeDeletedSubGroups);
        }

        groupHierarchy.removeGroup(group.getId(), user);
        groupHierarchyRepository.save(groupHierarchy);
    }

    @Transactional
    public void renameGroup(String groupId, IntegrationRenameGroupCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Rename", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        groupDomainService.rename(group, command.getName(), user);
        groupRepository.save(group);
        log.info("Integration renamed group[{}].", groupId);
    }

    @Transactional
    public void renameGroupByCustomId(String appId, String customId, IntegrationRenameGroupCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:Rename", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        groupDomainService.rename(group, command.getName(), user);
        groupRepository.save(group);
        log.info("Integration renamed group[appId={},customId={}].", appId, customId);
    }

    @Transactional
    public void updateGroupCustomId(String groupId, IntegrationUpdateGroupCustomIdCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:UpdateCustomId", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        groupDomainService.updateCustomId(group, command.getCustomId(), user);
        groupRepository.save(group);
        log.info("Integration updated custom ID for group[{}].", groupId);
    }

    @Transactional
    public void archiveGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Archive", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        doArchiveGroup(group, user);
        log.info("Integration archived group[{}].", groupId);
    }

    @Transactional
    public void archiveGroupByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:Archive", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        doArchiveGroup(group, user);
        log.info("Integration archived group[appId={},customId={}].", appId, customId);
    }

    private void doArchiveGroup(Group group, User user) {
        App app = appRepository.cachedById(group.getAppId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(group.getId());
        groupDomainService.checkArchiveGroups(app, concat(allSubGroupIds.stream(), of(group.getId())).collect(toImmutableSet()));

        group.archive(user);
        groupRepository.save(group);

        if (!allSubGroupIds.isEmpty()) {
            List<Group> groups = groupRepository.byIds(allSubGroupIds);
            groups.forEach(it -> it.archive(user));
            groupRepository.save(groups);
        }
    }

    @Transactional
    public void unArchiveGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:UnArchive", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        doUnArchiveGroup(group, user);
        log.info("Integration unArchived group[{}].", groupId);
    }

    @Transactional
    public void unArchiveGroupByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:UnArchive", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        doUnArchiveGroup(group, user);
        log.info("Integration unArchived group[appId={},customId={}].", appId, customId);
    }

    private void doUnArchiveGroup(Group group, User user) {
        App app = appRepository.cachedById(group.getAppId());
        group.unArchive(user);
        groupRepository.save(group);
        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(group.getId());
        if (!allSubGroupIds.isEmpty()) {
            List<Group> groups = groupRepository.byIds(allSubGroupIds);
            groups.forEach(it -> it.unArchive(user));
            groupRepository.save(groups);
        }
    }

    @Transactional
    public void addGroupMembers(String groupId, IntegrationAddGroupMembersCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:AddMembers", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        checkAllMembersExist(command.getMemberIds(), group.getTenantId());
        group.addMembers(command.getMemberIds(), user);
        groupRepository.save(group);
        log.info("Integration added members{} to group[{}].", command.getMemberIds(), groupId);
    }

    @Transactional
    public void addGroupMembersByCustomId(String appId, String customId, IntegrationCustomAddGroupMembersCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:AddMembers", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        List<String> memberIds = memberRepository.cachedMemberIdsForCustomIds(user.getTenantId(), command.getMemberCustomIds());
        if (isNotEmpty(memberIds)) {
            group.addMembers(memberIds, user);
            groupRepository.save(group);
        }
        log.info("Integration custom added members{} to group[appId={},customId={}].",
                command.getMemberCustomIds(), appId, customId);
    }

    @Transactional
    public void removeGroupMembers(String groupId, IntegrationRemoveGroupMembersCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:RemoveMembers", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        group.removeMembers(command.getMemberIds(), user);
        groupRepository.save(group);
        log.info("Integration removed members{} from group[{}].", command.getMemberIds(), groupId);
    }

    @Transactional
    public void removeGroupMembersByCustomId(String appId, String customId, IntegrationCustomRemoveGroupMembersCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:RemoveMembers", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        List<String> memberIds = memberRepository.cachedMemberIdsForCustomIds(user.getTenantId(), command.getMemberCustomIds());

        if (isNotEmpty(memberIds)) {
            group.removeMembers(memberIds, user);
            groupRepository.save(group);
        }
        log.info("Integration custom removed members{} from group[appId={},groupCustomId={}].",
                command.getMemberCustomIds(), appId, customId);
    }

    @Transactional
    public void addGroupManagers(String groupId, IntegrationAddGroupManagersCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:AddManagers", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        checkAllMembersExist(command.getMemberIds(), group.getTenantId());
        group.addManagers(command.getMemberIds(), user);
        groupRepository.save(group);
        log.info("Integration added managers{} to group[{}].", command.getMemberIds(), groupId);
    }

    @Transactional
    public void addGroupManagersByCustomId(String appId, String customId, IntegrationCustomAddGroupManagersCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:AddManagers", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        List<String> memberIds = memberRepository.cachedMemberIdsForCustomIds(user.getTenantId(), command.getMemberCustomIds());

        if (isNotEmpty(memberIds)) {
            group.addManagers(memberIds, user);
            groupRepository.save(group);
        }
        log.info("Integration custom added managers{} to group[appId={},groupCustomId={}].",
                command.getMemberCustomIds(), appId, customId);
    }

    @Transactional
    public void removeGroupManagers(String groupId, IntegrationRemoveGroupManagersCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:RemoveManagers", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        group.removeManagers(command.getMemberIds(), user);
        groupRepository.save(group);
        log.info("Integration removed managers{} from group[{}].", command.getMemberIds(), groupId);
    }

    @Transactional
    public void removeGroupManagersByCustomId(String appId, String customId, IntegrationCustomRemoveGroupManagersCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:RemoveManagers", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        List<String> memberIds = memberRepository.cachedMemberIdsForCustomIds(user.getTenantId(), command.getMemberCustomIds());

        if (isNotEmpty(memberIds)) {
            group.removeManagers(memberIds, user);
            groupRepository.save(group);
        }
        log.info("Integration custom removed managers{} from group[appId={},groupCustomId={}].",
                command.getMemberCustomIds(), appId, customId);
    }

    @Transactional
    public void activateGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Activate", 10);

        Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
        doActivateGroup(group, user);
        log.info("Integration activated group[{}].", groupId);
    }

    @Transactional
    public void activateGroupByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:Activate", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        doActivateGroup(group, user);
        log.info("Integration activated group[appId={},customId={}].", appId, customId);
    }

    private void doActivateGroup(Group group, User user) {
        App app = appRepository.cachedById(group.getAppId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        group.activate(user);
        groupRepository.save(group);

        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(group.getId());
        if (!allSubGroupIds.isEmpty()) {
            List<Group> groups = groupRepository.byIds(allSubGroupIds);
            groups.forEach(it -> it.activate(user));
            groupRepository.save(groups);
        }
    }

    @Transactional
    public void deactivateGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Deactivate", 10);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        doDeactivateGroup(group, user);
        log.info("Integration deactivated group[{}].", groupId);
    }

    @Transactional
    public void deactivateGroupByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:Group:Custom:Deactivate", 10);

        Group group = groupRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        doDeactivateGroup(group, user);
        log.info("Integration deactivated group[appId={},customId={}].", appId, customId);
    }

    private void doDeactivateGroup(Group group, User user) {
        App app = appRepository.cachedById(group.getAppId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(group.getId());
        groupDomainService.checkDeactivateGroups(app, concat(allSubGroupIds.stream(), of(group.getId())).collect(toImmutableSet()));

        group.deactivate(user);
        groupRepository.save(group);
        if (!allSubGroupIds.isEmpty()) {
            List<Group> groups = groupRepository.byIds(allSubGroupIds);
            groups.forEach(it -> it.deactivate(user));
            groupRepository.save(groups);
        }
    }

    private void checkAllMembersExist(List<String> memberIds, String tenantId) {
        if (memberRepository.cachedNotAllMembersExist(memberIds, tenantId)) {
            throw new MryException(NOT_ALL_MEMBERS_EXIST, "有成员不存在。", "tenantId", tenantId);
        }
    }

}
