package com.mryqr.core.platebatch.command;

import com.mryqr.common.domain.permission.ManagePermissionChecker;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.platebatch.domain.*;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlateBatchCommandService {
    private final PlateBatchFactory plateBatchFactory;
    private final PlateBatchRepository plateBatchRepository;
    private final AppRepository appRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final PlateBatchDomainService plateBatchDomainService;
    private final PlateRepository plateRepository;
    private final TenantRepository tenantRepository;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public String createPlateBatch(CreatePlateBatchCommand command, User user) {
        user.checkIsHumanUser();
        mryRateLimiter.applyFor(user.getTenantId(), "PlateBatch:Create", 5);

        App app = appRepository.cachedByIdAndCheckTenantShip(command.getAppId(), user);
        managePermissionChecker.checkCanManageApp(user, app);
        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(app.getTenantId());
        packagesStatus.validateAddPlate();

        CreatePlateBatchResult result = plateBatchFactory.create(command.getName(), command.getTotal(), app, user);
        PlateBatch plateBatch = result.getPlateBatch();
        plateBatchRepository.save(plateBatch);
        plateRepository.insert(result.getPlates());
        log.info("Created plate batch[{}].", plateBatch.getId());
        return plateBatch.getId();
    }

    @Transactional
    public void renamePlateBatch(String plateBatchId, RenamePlateBatchCommand command, User user) {
        user.checkIsHumanUser();
        mryRateLimiter.applyFor(user.getTenantId(), "PlateBatch:Rename", 5);

        PlateBatch plateBatch = plateBatchRepository.byIdAndCheckTenantShip(plateBatchId, user);
        managePermissionChecker.checkCanManagePlateBatch(user, plateBatch);

        String name = command.getName();
        if (Objects.equals(plateBatch.getName(), name)) {
            return;
        }

        plateBatchDomainService.rename(plateBatch, name, user);
        plateBatchRepository.save(plateBatch);
        log.info("Renamed plate batch[{}].", plateBatchId);
    }

    @Transactional
    public void deletePlateBatch(String plateBatchId, User user) {
        user.checkIsHumanUser();
        mryRateLimiter.applyFor(user.getTenantId(), "PlateBatch:Delete", 5);

        PlateBatch plateBatch = plateBatchRepository.byIdAndCheckTenantShip(plateBatchId, user);
        managePermissionChecker.checkCanManagePlateBatch(user, plateBatch);

        plateBatch.onDelete(user);
        plateBatchRepository.delete(plateBatch);
        log.info("Deleted plate batch[{}].", plateBatchId);
    }
}
