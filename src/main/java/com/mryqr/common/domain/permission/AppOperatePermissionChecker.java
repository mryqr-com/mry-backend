package com.mryqr.common.domain.permission;

import com.google.common.collect.ImmutableSet;
import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Stream.concat;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Component
@RequiredArgsConstructor
public class AppOperatePermissionChecker {
    private final ManagePermissionChecker managePermissionChecker;
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;

    public AppOperatePermissions permissionsFor(User user, App app) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(app, "App must not be null.");

        if (managePermissionChecker.canManageApp(user, app)) {
            return allPermissionResult(user, app);
        }

        switch (app.getOperationPermission()) {
            case CAN_MANAGE_APP -> {
                return noPermissionResult(user, app);
            }
            case CAN_MANAGE_GROUP -> {
                return forGroupManagerRequired(user, app);
            }
            case AS_GROUP_MEMBER -> {
                return forGroupMemberRequired(user, app);
            }
            default -> {
                return forTenantMemberRequired(user, app);
            }
        }
    }

    private AppOperatePermissions allPermissionResult(User user, App app) {
        List<AppCachedGroup> allGroups = groupRepository.cachedAppAllGroups(app.getId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(app.getId());
        Set<String> viewableGroupIds = allGroupIds(allGroups);

        Map<String, String> groupFullNames = groupFullNamesOf(allGroups, groupHierarchy, viewableGroupIds);
        List<Page> fillablePages = app.allFillablePages();
        Set<String> fillablePageIds = fillablePages.stream().map(Page::getId).collect(toImmutableSet());
        Set<String> viewablePageIds = fillablePages.stream().filter(Page::requireLogin).map(Page::getId).collect(toImmutableSet());
        Set<String> approvablePageIds = app.allApprovablePages().stream().map(Page::getId).collect(toImmutableSet());

        return AppOperatePermissions.builder()
                .user(user)
                .appId(app.getId())
                .canManageApp(true)
                .viewableGroupIds(viewableGroupIds)
                .viewablePageIds(viewablePageIds)
                .managableGroupIds(viewableGroupIds)
                .managablePageIds(fillablePageIds)
                .approvableGroupIds(viewableGroupIds)
                .approvablePageIds(approvablePageIds)
                .groupFullNames(groupFullNames)
                .build();
    }

    private AppOperatePermissions forGroupManagerRequired(User user, App app) {
        List<AppCachedGroup> allGroups = groupRepository.cachedAppAllGroups(app.getId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(app.getId());

        Set<String> asManagerGroupIds = asManagerGroupIds(user.getMemberId(), allGroups, groupHierarchy);
        if (isEmpty(asManagerGroupIds)) {
            return noPermissionResult(user, app);
        }

        Set<String> asMemberGroupIds = asMemberGroupIds(user.getMemberId(), allGroups, groupHierarchy);
        Set<String> viewableGroupIds = concat(asManagerGroupIds.stream(), asMemberGroupIds.stream()).collect(toImmutableSet());

        Map<String, String> groupFullNames = groupFullNamesOf(allGroups, groupHierarchy, viewableGroupIds);
        Permission maxPermission = maxPermission(asManagerGroupIds, asMemberGroupIds);
        List<Page> permissionedFillablePages = permissionedFillablePages(app, maxPermission);
        Set<String> fillablePageIds = permissionedFillablePages.stream().map(Page::getId).collect(toImmutableSet());
        Set<String> viewablePageIds = permissionedFillablePages.stream().filter(Page::requireLogin).map(Page::getId).collect(toImmutableSet());
        Set<String> approvablePageIds = approvablePageIds(app, maxPermission);

        return AppOperatePermissions.builder()
                .user(user)
                .appId(app.getId())
                .canManageApp(false)
                .viewableGroupIds(viewableGroupIds)
                .viewablePageIds(viewablePageIds)
                .managableGroupIds(asManagerGroupIds)
                .managablePageIds(isEmpty(asManagerGroupIds) ? Set.of() : fillablePageIds)
                .approvableGroupIds(isEmpty(approvablePageIds) ? Set.of() : asManagerGroupIds)
                .approvablePageIds(approvablePageIds)
                .groupFullNames(groupFullNames)
                .build();
    }

    private AppOperatePermissions forGroupMemberRequired(User user, App app) {
        List<AppCachedGroup> allGroups = groupRepository.cachedAppAllGroups(app.getId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(app.getId());

        Set<String> asManagerGroupIds = asManagerGroupIds(user.getMemberId(), allGroups, groupHierarchy);
        Set<String> asMemberGroupIds = asMemberGroupIds(user.getMemberId(), allGroups, groupHierarchy);
        Set<String> viewableGroupIds = concat(asManagerGroupIds.stream(), asMemberGroupIds.stream()).collect(toImmutableSet());

        if (isEmpty(viewableGroupIds)) {
            return noPermissionResult(user, app);
        }

        Map<String, String> groupFullNames = groupFullNamesOf(allGroups, groupHierarchy, viewableGroupIds);
        Permission maxPermission = maxPermission(asManagerGroupIds, asMemberGroupIds);
        List<Page> permissionedFillablePages = permissionedFillablePages(app, maxPermission);
        Set<String> fillablePageIds = permissionedFillablePages.stream().map(Page::getId).collect(toImmutableSet());
        Set<String> viewablePageIds = permissionedFillablePages.stream().filter(Page::requireLogin).map(Page::getId).collect(toImmutableSet());
        Set<String> approvablePageIds = approvablePageIds(app, maxPermission);

        return AppOperatePermissions.builder()
                .user(user)
                .appId(app.getId())
                .canManageApp(false)
                .viewableGroupIds(viewableGroupIds)
                .viewablePageIds(viewablePageIds)
                .managableGroupIds(asManagerGroupIds)
                .managablePageIds(isEmpty(asManagerGroupIds) ? Set.of() : fillablePageIds)
                .approvableGroupIds(isEmpty(approvablePageIds) ? Set.of() : asManagerGroupIds)
                .approvablePageIds(approvablePageIds)
                .groupFullNames(groupFullNames)
                .build();
    }

    private AppOperatePermissions forTenantMemberRequired(User user, App app) {
        List<AppCachedGroup> allGroups = groupRepository.cachedAppAllGroups(app.getId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(app.getId());

        Set<String> asManagerGroupIds = asManagerGroupIds(user.getMemberId(), allGroups, groupHierarchy);
        Set<String> asMemberGroupIds = asMemberGroupIds(user.getMemberId(), allGroups, groupHierarchy);
        Set<String> viewableGroupIds = allGroupIds(allGroups);

        Map<String, String> groupFullNames = groupFullNamesOf(allGroups, groupHierarchy, viewableGroupIds);
        Permission maxPermission = maxPermission(asManagerGroupIds, asMemberGroupIds);
        List<Page> permissionedFillablePages = permissionedFillablePages(app, maxPermission);
        Set<String> fillablePageIds = permissionedFillablePages.stream().map(Page::getId).collect(toImmutableSet());
        Set<String> viewablePageIds = permissionedFillablePages.stream().filter(Page::requireLogin).map(Page::getId).collect(toImmutableSet());
        Set<String> approvablePageIds = approvablePageIds(app, maxPermission);

        return AppOperatePermissions.builder()
                .user(user)
                .appId(app.getId())
                .canManageApp(false)
                .viewableGroupIds(viewableGroupIds)
                .viewablePageIds(viewablePageIds)
                .managableGroupIds(asManagerGroupIds)
                .managablePageIds(isEmpty(asManagerGroupIds) ? Set.of() : fillablePageIds)
                .approvableGroupIds(isEmpty(approvablePageIds) ? Set.of() : asManagerGroupIds)
                .approvablePageIds(approvablePageIds)
                .groupFullNames(groupFullNames)
                .build();
    }

    private AppOperatePermissions noPermissionResult(User user, App app) {
        Set<String> empty = Set.of();
        return AppOperatePermissions.builder()
                .user(user)
                .appId(app.getId())
                .canManageApp(false)
                .viewableGroupIds(empty)
                .viewablePageIds(empty)
                .managableGroupIds(empty)
                .managablePageIds(empty)
                .approvableGroupIds(empty)
                .approvablePageIds(empty)
                .build();
    }

    private ImmutableSet<String> allGroupIds(List<AppCachedGroup> allGroups) {
        return allGroups.stream()
                .filter(AppCachedGroup::isVisible)
                .map(AppCachedGroup::getId)
                .collect(toImmutableSet());
    }

    private Set<String> asManagerGroupIds(String memberId, List<AppCachedGroup> allGroups, GroupHierarchy groupHierarchy) {
        Set<String> asManagerGroupIds = allGroups.stream()
                .filter(group -> group.containsManager(memberId))
                .map(group -> groupHierarchy.withAllSubGroupIdsOf(group.getId()))
                .flatMap(Collection::stream)
                .collect(toImmutableSet());

        return allGroups.stream()
                .filter(group -> group.isVisible() && asManagerGroupIds.contains(group.getId()))
                .map(AppCachedGroup::getId)
                .collect(toImmutableSet());
    }

    private Set<String> asMemberGroupIds(String memberId, List<AppCachedGroup> allGroups, GroupHierarchy groupHierarchy) {
        Set<String> asMemberGroupIds = allGroups.stream()
                .filter(group -> group.containsMember(memberId))
                .map(group -> groupHierarchy.withAllParentGroupIdsOf(group.getId()))
                .flatMap(Collection::stream)
                .collect(toImmutableSet());

        return allGroups.stream()
                .filter(group -> group.isVisible() && asMemberGroupIds.contains(group.getId()))
                .map(AppCachedGroup::getId)
                .collect(toImmutableSet());
    }

    private Permission maxPermission(Set<String> asManagerGroups, Set<String> asMemberGroups) {
        if (isNotEmpty(asManagerGroups)) {
            return Permission.CAN_MANAGE_GROUP;
        }

        if (isNotEmpty(asMemberGroups)) {
            return Permission.AS_GROUP_MEMBER;
        }

        return Permission.AS_TENANT_MEMBER;
    }

    private List<Page> permissionedFillablePages(App app, Permission givenPermission) {
        return app.allFillablePages().stream()
                .filter(page -> givenPermission.covers(page.requiredPermission()))
                .collect(toImmutableList());
    }

    private Set<String> approvablePageIds(App app, Permission givenPermission) {
        return app.allApprovablePages().stream()
                .filter(Page::isApprovalEnabled)
                .filter(page -> givenPermission.covers(page.requiredApprovalPermission()) && givenPermission.covers(page.requiredPermission()))
                .map(Page::getId).collect(toImmutableSet());
    }

    private Map<String, String> groupFullNamesOf(List<AppCachedGroup> allGroups, GroupHierarchy groupHierarchy, Set<String> groupIds) {
        Map<String, String> allGroupNames = allGroups.stream().collect(toImmutableMap(AppCachedGroup::getId, AppCachedGroup::getName));
        return groupHierarchy.groupFullNames(allGroupNames).entrySet().stream()
                .filter(entry -> groupIds.contains(entry.getKey()))
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
