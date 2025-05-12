package com.mryqr.core.grouphierarchy.command;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.permission.ManagePermissionChecker;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyDomainService;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupHierarchyCommandService {
    private final AppRepository appRepository;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final GroupHierarchyDomainService groupHierarchyDomainService;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public void updateGroupHierarchy(String appId, UpdateGroupHierarchyCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "GroupHierarchy:Update", 5);

        GroupHierarchy groupHierarchy = groupHierarchyRepository.byAppIdAndCheckTenantShip(appId, user);
        App app = appRepository.cachedById(appId);
        managePermissionChecker.checkCanManageApp(user, app);

        groupHierarchyDomainService.updateGroupHierarchy(groupHierarchy, command.getIdTree(), user);
        groupHierarchyRepository.save(groupHierarchy);
        log.info("Updated group hierarchy for app[{}].", appId);
    }
}
