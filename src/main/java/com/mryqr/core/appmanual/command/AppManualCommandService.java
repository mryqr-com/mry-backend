package com.mryqr.core.appmanual.command;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.appmanual.domain.AppManual;
import com.mryqr.core.appmanual.domain.AppManualFactory;
import com.mryqr.core.appmanual.domain.AppManualRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppManualCommandService {
    private final MryRateLimiter mryRateLimiter;
    private final AppManualRepository appManualRepository;
    private final AppRepository appRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final AppManualFactory appManualFactory;

    @Transactional
    public void updateAppManual(String appId, UpdateAppManualCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "AppManual:Update", 5);

        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        Optional<AppManual> appManualOptional = appManualRepository.byAppIdOptional(appId);
        AppManual appManual;
        if (appManualOptional.isPresent()) {
            appManual = appManualOptional.get();
            appManual.updateContent(command.getContent(), user);
        } else {
            appManual = appManualFactory.create(app, command.getContent(), user);
        }

        appManualRepository.save(appManual);
        log.info("Updated app manual for app[{}].", appId);
    }
}
