package com.mryqr.core.qr.query.bindplate;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.group.domain.AppCachedGroup;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.MryException.accessDeniedException;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class BindPlateQueryService {
    private final AppRepository appRepository;
    private final PlateRepository plateRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final GroupHierarchyRepository groupHierarchyRepository;
    private final GroupRepository groupRepository;
    private final MryRateLimiter mryRateLimiter;

    public QBindPlateInfo fetchBindQrPlateInfo(String plateId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:FetchBindPlateInfo", 20);

        Plate plate = plateRepository.byIdAndCheckTenantShip(plateId, user);
        App app = appRepository.cachedById(plate.getAppId());

        List<AppCachedGroup> allGroups = groupRepository.cachedAppAllGroups(app.getId());
        Map<String, String> allGroupNames = allGroups.stream().collect(toImmutableMap(AppCachedGroup::getId, AppCachedGroup::getName));
        GroupHierarchy groupHierarchy = groupHierarchyRepository.cachedByAppId(app.getId());

        Set<String> asManagerGroupIds = managePermissionChecker.canManageApp(user, app) ?
                allGroups.stream()
                        .filter(AppCachedGroup::isVisible)
                        .map(AppCachedGroup::getId)
                        .collect(toImmutableSet()) :
                allGroups.stream()
                        .filter(group -> group.isVisible() && group.containsManager(user.getMemberId()))
                        .map(group -> groupHierarchy.withAllSubGroupIdsOf(group.getId()))
                        .flatMap(Collection::stream)
                        .collect(toImmutableSet());

        Map<String, String> groupFullNames = groupHierarchy.groupFullNames(allGroupNames).entrySet().stream()
                .filter(entry -> asManagerGroupIds.contains(entry.getKey()))
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));

        if (isEmpty(asManagerGroupIds)) {
            throw accessDeniedException();
        }

        plate.checkBindStatus();

        return QBindPlateInfo.builder()
                .plateId(plateId)
                .memberId(user.getMemberId())
                .appId(app.getId())
                .appName(app.getName())
                .instanceDesignation(app.instanceDesignation())
                .groupDesignation(app.groupDesignation())
                .homePageId(app.homePageId())
                .selectableGroups(groupFullNames)
                .build();
    }
}
