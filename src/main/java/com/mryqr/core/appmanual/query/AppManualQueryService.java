package com.mryqr.core.appmanual.query;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.appmanual.domain.AppManualRepository;
import com.mryqr.core.common.domain.permission.AppOperatePermissionChecker;
import com.mryqr.core.common.domain.permission.AppOperatePermissions;
import com.mryqr.core.common.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppManualQueryService {
    private final MryRateLimiter mryRateLimiter;
    private final AppManualRepository appManualRepository;
    private final AppRepository appRepository;
    private final AppOperatePermissionChecker appOperatePermissionChecker;

    public QAppManual fetchAppManual(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AppManual:Fetch", 20);

        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        AppOperatePermissions permissions = appOperatePermissionChecker.permissionsFor(user, app);
        permissions.checkHasPermissions();

        return appManualRepository.byAppIdOptional(app.getId())
                .map(appManual -> QAppManual.builder()
                        .id(appManual.getId())
                        .appId(appManual.getAppId())
                        .content(appManual.getContent())
                        .build())
                .orElse(null);
    }
}
