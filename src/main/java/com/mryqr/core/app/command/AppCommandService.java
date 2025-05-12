package com.mryqr.core.app.command;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.webhook.consume.WebhookCallService;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppDomainService;
import com.mryqr.core.app.domain.AppFactory;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.CreateAppResult;
import com.mryqr.core.app.domain.UpdateAppSettingResult;
import com.mryqr.core.app.domain.page.control.ControlType;
import com.mryqr.core.app.domain.report.ReportSetting;
import com.mryqr.core.common.domain.permission.ManagePermissionChecker;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchy;
import com.mryqr.core.grouphierarchy.domain.GroupHierarchyRepository;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Set;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppCommandService {
    private final AppRepository appRepository;
    private final GroupRepository groupRepository;
    private final AppFactory appFactory;
    private final AppDomainService appDomainService;
    private final ManagePermissionChecker managePermissionChecker;
    private final TenantRepository tenantRepository;
    private final MryRateLimiter mryRateLimiter;
    private final QrRepository qrRepository;
    private final WebhookCallService webhookCallService;
    private final GroupHierarchyRepository groupHierarchyRepository;

    @Transactional
    public CreateAppResponse createApp(CreateAppCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "App:Create", 1);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateAddApp();

        CreateAppResult result = appFactory.create(command.getName(), user);
        App app = result.getApp();
        appRepository.save(app);

        Group defaultGroup = result.getDefaultGroup();
        groupRepository.save(defaultGroup);

        GroupHierarchy groupHierarchy = result.getGroupHierarchy();
        groupHierarchyRepository.save(groupHierarchy);
        log.info("Created app[{}].", app.getId());

        return CreateAppResponse.builder()
                .appId(app.getId())
                .defaultGroupId(defaultGroup.getId())
                .homePageId(app.homePageId())
                .build();
    }

    @Transactional
    public CreateAppResponse copyApp(CopyAppCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "App:Copy", 1);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateAddApp();

        App app = appRepository.byIdAndCheckTenantShip(command.getSourceAppId(), user);
        packagesStatus.validateCopyApp(app);

        CreateAppResult result = appFactory.copyFrom(app, command.getName(), user);

        App copiedApp = result.getApp();
        appRepository.save(copiedApp);

        Group defaultGroup = result.getDefaultGroup();
        groupRepository.save(defaultGroup);

        GroupHierarchy groupHierarchy = result.getGroupHierarchy();
        groupHierarchyRepository.save(groupHierarchy);
        log.info("Copied app[{}] to app[{}].", command.getSourceAppId(), copiedApp.getId());

        return CreateAppResponse.builder()
                .appId(copiedApp.getId())
                .defaultGroupId(defaultGroup.getId())
                .homePageId(copiedApp.homePageId())
                .build();
    }

    @Transactional
    public CreateAppResponse createAppFromTemplate(String appTemplateId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "App:CreateFromTemplate", 1);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateAddApp();

        QR appTemplate = qrRepository.byId(appTemplateId);
        packagesStatus.validCreateAppFromTemplate(appTemplate);

        App templateApp = appRepository.byId(appTemplate.getCustomId());
        CreateAppResult result = appFactory.createFromTemplate(appTemplate, templateApp, user);

        App newApp = result.getApp();
        appRepository.save(newApp);

        Group defaultGroup = result.getDefaultGroup();
        groupRepository.save(defaultGroup);

        GroupHierarchy groupHierarchy = result.getGroupHierarchy();
        groupHierarchyRepository.save(groupHierarchy);
        log.info("Created app[{}] from app template[{}].", newApp.getId(), appTemplateId);

        return CreateAppResponse.builder()
                .appId(newApp.getId())
                .defaultGroupId(defaultGroup.getId())
                .homePageId(newApp.homePageId())
                .build();
    }

    @Transactional
    public void renameApp(String appId, RenameAppCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:Rename", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        String name = command.getName();
        if (Objects.equals(app.getName(), name)) {
            return;
        }

        appDomainService.renameApp(app, name, user);
        appRepository.save(app);
        log.info("Renamed app[{}].", appId);
    }

    @Transactional
    public void activateApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:Activate", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        app.activate(user);
        appRepository.save(app);
        log.info("Activated app[{}].", appId);
    }

    @Transactional
    public void deactivateApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:Deactivate", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        app.deactivate(user);
        appRepository.save(app);
        log.info("Deactivated app[{}].", appId);
    }

    @Transactional
    public void lockApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:Lock", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        app.lock(user);
        appRepository.save(app);
        log.info("Locked app[{}].", appId);
    }

    @Transactional
    public void unlockApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:Unlock", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        app.unlock(user);
        appRepository.save(app);
        log.info("Unlocked app[{}].", appId);
    }

    @Transactional
    public void setAppManagers(String appId, SetAppManagersCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:SetManagers", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        appDomainService.setManagers(app, command.getManagers(), user);
        appRepository.save(app);
        log.info("Set managers for app[{}]:{}.", appId, command.getManagers());
    }

    @Transactional
    public String updateAppSetting(String appId, UpdateAppSettingCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:UpdateSetting", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        AppSetting setting = command.getSetting();
        if (Objects.equals(app.getSetting(), setting)) {
            return app.getVersion();
        }

        UpdateAppSettingResult updateResult = app.updateSetting(setting, command.getVersion(), user);

        Set<ControlType> newlyAddedControlTypes = updateResult.getNewlyAddedControlTypes();
        if (isNotEmpty(newlyAddedControlTypes)) {
            PackagesStatus packagesStatus = tenantRepository.cachedById(app.getTenantId()).packagesStatus();
            packagesStatus.validateAppNewlyAddControls(newlyAddedControlTypes);
        }

        appRepository.save(app);
        log.info("Updated setting for app[{}] to version[{}].", appId, app.getVersion());

        return app.getVersion();
    }

    @Transactional
    public void deleteApp(String appId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "App:DeleteApp", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        app.onDelete(user);
        appRepository.delete(app);
        log.info("Deleted app[{}].", appId);
    }

    @Transactional
    public void updateAppReportSetting(String appId, UpdateAppReportSettingCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:UpdateReportSetting", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);
        PackagesStatus packagesStatus = tenantRepository.cachedById(app.getTenantId()).packagesStatus();
        packagesStatus.validateReporting();

        ReportSetting setting = command.getSetting();
        if (Objects.equals(setting, app.getReportSetting())) {
            return;
        }

        app.updateReportSetting(setting, user);
        appRepository.save(app);
        log.info("Updated report setting for app[{}].", appId);
    }

    @Transactional
    public void updateAppWebhookSetting(String appId, UpdateAppWebhookSettingCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:UpdateWebhookSetting", 5);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);
        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(app.getTenantId());
        packagesStatus.validateUpdateWebhookSetting();

        webhookCallService.resetFailureCountFor(appId);
        app.updateWebhookSetting(command.getWebhookSetting(), user);
        appRepository.save(app);
        log.info("Updated webhook setting for app[{}].", appId);
    }

    @Transactional
    public void enableGroupSync(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "App:EnableGroupSync", 1);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        app.enableGroupSync(user);
        appRepository.save(app);
        log.info("Enabled group sync for app[{}].", appId);
    }
}
