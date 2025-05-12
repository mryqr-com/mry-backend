package com.mryqr.core.group.command;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
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
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.NOT_ALL_MEMBERS_EXIST;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupCommandService {
    private final GroupFactory groupFactory;
    private final GroupRepository groupRepository;
    private final GroupDomainService groupDomainService;
    private final ManagePermissionChecker managePermissionChecker;
    private final AppRepository appRepository;
    private final TenantRepository tenantRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final MryRateLimiter mryRateLimiter;
    private final MemberRepository memberRepository;

    @Transactional
    public String createGroup(CreateGroupCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:Create", 5);

        String appId = command.getAppId();
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(app.getTenantId());
        packagesStatus.validateAddGroup(appId);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(appId);
        Group group = groupFactory.create(command.getName(), command.getParentGroupId(), groupHierarchy, app, user);
        groupHierarchy.addGroup(command.getParentGroupId(), group.getId(), user);

        groupRepository.save(group);
        groupHierarchyRepository.save(groupHierarchy);
        log.info("Created group[{}] under app[{}].", group.getId(), command.getAppId());

        return group.getId();
    }

    @Transactional
    public void renameGroup(String groupId, RenameGroupCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:Rename", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        String name = command.getName();
        if (Objects.equals(group.getName(), name)) {
            return;
        }

        groupDomainService.rename(group, name, user);
        groupRepository.save(group);
        log.info("Renamed group[{}].", groupId);
    }

    @Transactional
    public void deleteGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:Delete", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(groupId);
        groupDomainService.checkDeleteGroups(app, concat(allSubGroupIds.stream(), of(groupId)).collect(toImmutableSet()));

        group.onDelete(user);
        groupRepository.delete(group);

        if (!allSubGroupIds.isEmpty()) {
            List<Group> allTobeDeletedSubGroups = groupRepository.byIds(allSubGroupIds);
            allTobeDeletedSubGroups.forEach(it -> it.onDelete(user));
            groupRepository.delete(allTobeDeletedSubGroups);
        }

        groupHierarchy.removeGroup(group.getId(), user);
        groupHierarchyRepository.save(groupHierarchy);
        log.info("Deleted group[{}].", groupId);
    }

    @Transactional
    public void archiveGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:Archive", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(groupId);
        groupDomainService.checkArchiveGroups(app, concat(allSubGroupIds.stream(), of(groupId)).collect(toImmutableSet()));

        group.archive(user);
        groupRepository.save(group);

        if (!allSubGroupIds.isEmpty()) {
            List<Group> groups = groupRepository.byIds(allSubGroupIds);
            groups.forEach(it -> it.archive(user));
            groupRepository.save(groups);
        }
        log.info("Archived group[{}].", groupId);
    }

    @Transactional
    public void unArchiveGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:UnArchive", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        group.unArchive(user);
        groupRepository.save(group);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(groupId);
        if (!allSubGroupIds.isEmpty()) {
            List<Group> groups = groupRepository.byIds(allSubGroupIds);
            groups.forEach(it -> it.unArchive(user));
            groupRepository.save(groups);
        }

        log.info("UnArchived group[{}].", groupId);
    }

    @Transactional
    public void activateGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:Activate", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        group.activate(user);
        groupRepository.save(group);

        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(groupId);
        if (!allSubGroupIds.isEmpty()) {
            List<Group> groups = groupRepository.byIds(allSubGroupIds);
            groups.forEach(it -> it.activate(user));
            groupRepository.save(groups);
        }
        log.info("Activated group[{}].", groupId);
    }

    @Transactional
    public void deactivateGroup(String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:Deactivate", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppId(app.getId());
        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(groupId);
        groupDomainService.checkDeactivateGroups(app, concat(allSubGroupIds.stream(), of(groupId)).collect(toImmutableSet()));

        group.deactivate(user);
        groupRepository.save(group);
        if (!allSubGroupIds.isEmpty()) {
            List<Group> groups = groupRepository.byIds(allSubGroupIds);
            groups.forEach(it -> it.deactivate(user));
            groupRepository.save(groups);
        }

        log.info("Deactivated group[{}].", groupId);
    }

    @Transactional
    public void addGroupMembers(String groupId, List<String> memberIds, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:AddMembers", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        checkAllMembersExist(memberIds, group.getTenantId());
        group.addMembers(memberIds, user);
        groupRepository.save(group);
        log.info("Added members{} to group[{}].", memberIds, groupId);
    }

    @Transactional
    public void addGroupManagers(String groupId, List<String> memberIds, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:AddManagers", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        checkAllMembersExist(memberIds, group.getTenantId());
        group.addManagers(memberIds, user);
        groupRepository.save(group);
        log.info("Added managers{} to group[{}].", memberIds, groupId);
    }

    @Transactional
    public void removeGroupMember(String groupId, String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:RemoveMember", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        group.removeMember(memberId, user);
        groupRepository.save(group);
        log.info("Removed member[{}] from group[{}].", memberId, groupId);
    }

    @Transactional
    public void addGroupManager(String groupId, String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:AddManager", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        Member member = memberRepository.cachedByIdAndCheckTenantShip(memberId, user);
        group.addManager(member.getId(), user);
        groupRepository.save(group);
        log.info("Added manager[{}] to group[{}].", memberId, groupId);
    }

    @Transactional
    public void removeGroupManager(String groupId, String memberId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Group:RemoveManager", 5);

        Group group = groupRepository.byIdAndCheckTenantShip(groupId, user);
        App app = appRepository.cachedById(group.getAppId());
        managePermissionChecker.checkCanManageApp(user, app);

        group.removeManager(memberId, user);
        groupRepository.save(group);
        log.info("Removed manager[{}] from group[{}].", memberId, groupId);
    }

    private void checkAllMembersExist(List<String> memberIds, String tenantId) {
        if (memberRepository.cachedNotAllMembersExist(memberIds, tenantId)) {
            throw new MryException(NOT_ALL_MEMBERS_EXIST, "有成员不存在。", "tenantId", tenantId);
        }
    }

}
