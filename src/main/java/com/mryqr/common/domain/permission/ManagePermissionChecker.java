package com.mryqr.common.domain.permission;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.platebatch.domain.PlateBatch;
import com.mryqr.core.qr.domain.QR;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.mryqr.common.exception.MryException.accessDeniedException;
import static java.util.Objects.requireNonNull;

@Component
@RequiredArgsConstructor
public class ManagePermissionChecker {
    private final GroupRepository groupRepository;
    private final AppRepository appRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;

    public boolean canManageApp(User user, App app) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(app, "App must not be null.");

        return user.isTenantRootFor(app.getTenantId()) || app.containsManager(user.getMemberId());
    }

    public void checkCanManageApp(User user, App app) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(app, "App must not be null.");

        if (!canManageApp(user, app)) {
            throw accessDeniedException();
        }
    }

    public boolean canManageGroup(User user, Group group) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(group, "Group must not be null.");

        if (user.isTenantRootFor(group.getTenantId())) {
            return true;
        }

        if (group.containsManager(user.getMemberId())) {
            return true;
        }

        App app = appRepository.cachedById(group.getAppId());
        if (app.containsManager(user.getMemberId())) {
            return true;
        }

        return canManageParentGroups(user, group);
    }

    public boolean canManageGroup(User user, Group group, App app) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(group, "Group must not be null.");
        requireNonNull(app, "App must not be null.");

        if (!group.getAppId().equals(app.getId())) {
            return false;
        }

        if (user.isTenantRootFor(group.getTenantId())) {
            return true;
        }

        if (group.containsManager(user.getMemberId())) {
            return true;
        }

        if (app.containsManager(user.getMemberId())) {
            return true;
        }

        return canManageParentGroups(user, group);
    }

    public void checkCanManageGroup(User user, Group group) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(group, "Group must not be null.");

        if (!canManageGroup(user, group)) {
            throw accessDeniedException();
        }
    }

    public void checkCanManageGroup(User user, Group group, App app) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(group, "Group must not be null.");
        requireNonNull(app, "App must not be null.");

        if (!canManageGroup(user, group, app)) {
            throw accessDeniedException();
        }
    }

    public boolean canManageQr(User user, QR qr) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(qr, "QR must not be null.");

        if (user.isTenantRootFor(qr.getTenantId())) {
            return true;
        }

        Group group = groupRepository.cachedById(qr.getGroupId());
        if (group.containsManager(user.getMemberId())) {
            return true;
        }

        App app = appRepository.cachedById(group.getAppId());
        if (app.containsManager(user.getMemberId())) {
            return true;
        }

        return canManageParentGroups(user, group);
    }

    public void checkCanManageQr(User user, QR qr) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(qr, "QR must not be null.");

        if (!canManageQr(user, qr)) {
            throw accessDeniedException();
        }
    }

    public boolean canManageQr(User user, QR qr, App app) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(qr, "QR must not be null.");
        requireNonNull(app, "App must not be null.");

        if (!qr.getAppId().equals(app.getId())) {
            return false;
        }

        if (user.isTenantRootFor(qr.getTenantId())) {
            return true;
        }

        if (app.containsManager(user.getMemberId())) {
            return true;
        }

        Group group = groupRepository.cachedById(qr.getGroupId());
        if (group.containsManager(user.getMemberId())) {
            return true;
        }

        return canManageParentGroups(user, group);
    }

    public void checkCanManageQr(User user, QR qr, App app) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(qr, "QR must not be null.");
        requireNonNull(app, "App must not be null.");

        if (!canManageQr(user, qr, app)) {
            throw accessDeniedException();
        }
    }

    public boolean canManagePlateBatch(User user, PlateBatch plateBatch) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(plateBatch, "Plate batch must not be null.");

        if (user.isTenantRootFor(plateBatch.getTenantId())) {
            return true;
        }

        App app = appRepository.cachedById(plateBatch.getAppId());
        return canManageApp(user, app);
    }

    public void checkCanManagePlateBatch(User user, PlateBatch plateBatch) {
        requireNonNull(user, "User must not be null.");
        requireNonNull(plateBatch, "Plate batch must not be null.");

        if (!canManagePlateBatch(user, plateBatch)) {
            throw accessDeniedException();
        }
    }

    private boolean canManageParentGroups(User user, Group group) {
        List<AppCachedGroup> allGroups = groupRepository.cachedAppAllGroups(group.getAppId());
        GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(group.getAppId());
        Set<String> allParentGroupIds = groupHierarchy.allParentGroupIdsOf(group.getId());

        return allGroups.stream()
                .filter(aGroup -> allParentGroupIds.contains(aGroup.getId()))
                .anyMatch(aGroup -> aGroup.containsManager(user.getMemberId()));
    }
}
