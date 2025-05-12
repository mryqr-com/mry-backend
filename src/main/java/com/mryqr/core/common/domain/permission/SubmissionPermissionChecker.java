package com.mryqr.core.common.domain.permission;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.qr.domain.AppedQr;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.domain.permission.Permission.AS_GROUP_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.AS_TENANT_MEMBER;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_APP;
import static com.mryqr.core.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.common.domain.permission.Permission.PUBLIC;
import static com.mryqr.core.common.domain.permission.Permission.maxPermission;
import static com.mryqr.core.common.exception.MryException.accessDeniedException;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static java.util.Objects.requireNonNull;


@Component
@RequiredArgsConstructor
public class SubmissionPermissionChecker {
    private final GroupRepository groupRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final ManagePermissionChecker managePermissionChecker;

    public SubmissionPermissions permissionsFor(User user, AppedQr appedQr) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(appedQr, "AppedQR must not be null.");

        return permissionsFor(user, appedQr.getApp(), appedQr.getQr().getGroupId());
    }

    public SubmissionPermissions permissionsFor(User user, App app, String groupId) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(app, "App must not be null.");
        requireNonBlank(groupId, "Group ID must not be blank.");

        Set<Permission> permissions = calculatePermissions(user, app, groupId);

        Set<String> canViewFillablePageIds = app.allFillablePages().stream()
                .filter(page -> page.requireLogin() && user.isLoggedInFor(app.getTenantId()) && permissions.contains(page.requiredPermission()))
                .map(Page::getId).collect(toImmutableSet());

        Set<String> canManageFillablePageIds = app.allFillablePages().stream()
                .filter(page -> (permissions.contains(CAN_MANAGE_GROUP) || permissions.contains(CAN_MANAGE_APP))
                        && permissions.contains(page.requiredPermission()))
                .map(Page::getId).collect(toImmutableSet());

        Set<String> canApproveFillablePageIds = app.allApprovablePages().stream()
                .filter(page -> permissions.contains(page.requiredPermission())
                        && permissions.contains(page.requiredApprovalPermission()))
                .map(Page::getId).collect(toImmutableSet());

        return SubmissionPermissions.builder()
                .user(user)
                .permissions(permissions)
                .canViewFillablePageIds(canViewFillablePageIds)
                .canManageFillablePageIds(canManageFillablePageIds)
                .canApproveFillablePageIds(canApproveFillablePageIds)
                .build();
    }

    private Set<Permission> calculatePermissions(User user, App app, String groupId) {
        //未登录，或者已登录但是tenant不一致，则只有PUBLIC权限
        if (!user.isLoggedInFor(app.getTenantId())) {
            return Set.of(PUBLIC);
        }

        //可以管理app，则拥有所有权限
        if (managePermissionChecker.canManageApp(user, app)) {
            return Set.of(Permission.values());
        }

        Group group = groupRepository.cachedById(groupId);

        //为group管理员
        if (group.isActive() && group.containsManager(user.getMemberId())) {
            return Set.of(PUBLIC, AS_TENANT_MEMBER, AS_GROUP_MEMBER, CAN_MANAGE_GROUP);
        }

        List<AppCachedGroup> allGroups = groupRepository.cachedAppAllGroups(group.getAppId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(group.getAppId());

        //为parent group管理员
        Set<String> allParentGroupIds = groupHierarchy.allParentGroupIdsOf(group.getId());
        if (allGroups.stream()
                .filter(aGroup -> allParentGroupIds.contains(aGroup.getId()))
                .anyMatch(aGroup -> aGroup.isVisible() && aGroup.containsManager(user.getMemberId()))) {
            return Set.of(PUBLIC, AS_TENANT_MEMBER, AS_GROUP_MEMBER, CAN_MANAGE_GROUP);
        }

        //为group成员
        if (group.containsMember(user.getMemberId())) {
            return Set.of(PUBLIC, AS_TENANT_MEMBER, AS_GROUP_MEMBER);
        }

        //为sub group成员
        Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(group.getId());
        if (allGroups.stream()
                .filter(aGroup -> allSubGroupIds.contains(aGroup.getId()))
                .anyMatch(aGroup -> aGroup.isVisible() && aGroup.containsMember(user.getMemberId()))) {
            return Set.of(PUBLIC, AS_TENANT_MEMBER, AS_GROUP_MEMBER);
        }

        return Set.of(PUBLIC, AS_TENANT_MEMBER);
    }

    public void checkPermissions(User user, AppedQr appedQr, Permission... requestedPermissions) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(appedQr, "AppedQR must not be null.");

        if (requestedPermissions.length == 0) {
            return;
        }

        Permission maxRequestedPermission = maxPermission(requestedPermissions);

        if (maxRequestedPermission.isPublic()) {
            return;
        }

        user.checkIsLoggedInFor(appedQr.getApp().getTenantId());

        if (!hasPermission(user, appedQr, maxRequestedPermission)) {
            throw accessDeniedException();
        }
    }

    private boolean hasPermission(User user, AppedQr appedQr, Permission requestedPermission) {
        switch (requestedPermission) {
            case PUBLIC -> {
                return true;
            }

            case AS_TENANT_MEMBER -> {
                return user.isLoggedInFor(appedQr.getApp().getTenantId());
            }

            case AS_GROUP_MEMBER -> {
                if (managePermissionChecker.canManageApp(user, appedQr.getApp())) {
                    return true;
                }

                Group group = groupRepository.cachedById(appedQr.getQr().getGroupId());
                if (group.isVisible() && group.containsMember(user.getMemberId())) {
                    return true;
                }

                List<AppCachedGroup> allGroups = groupRepository.cachedAppAllGroups(group.getAppId());
                GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(group.getAppId());
                Set<String> allSubGroupIds = groupHierarchy.allSubGroupIdsOf(group.getId());
                return allGroups.stream()
                        .filter(aGroup -> allSubGroupIds.contains(aGroup.getId()))
                        .anyMatch(aGroup -> aGroup.isVisible() && aGroup.containsMember(user.getMemberId()));
            }

            case CAN_MANAGE_GROUP -> {
                if (managePermissionChecker.canManageApp(user, appedQr.getApp())) {
                    return true;
                }

                Group group = groupRepository.cachedById(appedQr.getQr().getGroupId());
                if (group.containsManager(user.getMemberId())) {
                    return true;
                }

                List<AppCachedGroup> allGroups = groupRepository.cachedAppAllGroups(group.getAppId());
                GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(group.getAppId());
                Set<String> allParentGroupIds = groupHierarchy.allParentGroupIdsOf(group.getId());
                return allGroups.stream()
                        .filter(aGroup -> allParentGroupIds.contains(aGroup.getId()))
                        .anyMatch(aGroup -> aGroup.isVisible() && aGroup.containsManager(user.getMemberId()));
            }

            case CAN_MANAGE_APP -> {
                return managePermissionChecker.canManageApp(user, appedQr.getApp());
            }

            default -> {
                return false;
            }
        }
    }

}
