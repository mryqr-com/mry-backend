package com.mryqr.core.grouphierarchy.query;


import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.grouphierarchy.query.QGroupHierarchy.QHierarchyGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

@Component
@RequiredArgsConstructor
public class GroupHierarchyQueryService {
    private final AppRepository appRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final GroupRepository groupRepository;
    private final MryRateLimiter mryRateLimiter;

    public QGroupHierarchy fetchGroupHierarchy(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "GroupHierarchy:Fetch", 10);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppIdAndCheckTenantShip(appId, user);
        App app = appRepository.cachedById(appId);
        managePermissionChecker.checkCanManageApp(user, app);

        List<QHierarchyGroup> groups = groupRepository.cachedAppAllGroups(appId).stream()
                .map(group -> QHierarchyGroup.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .active(group.isActive())
                        .archived(group.isArchived())
                        .sync(group.isSynced())
                        .build())
                .collect(toImmutableList());

        return QGroupHierarchy.builder()
                .idTree(groupHierarchy.getIdTree())
                .allGroups(groups)
                .sync(app.isGroupSynced())
                .build();
    }
}
