package com.mryqr.core.qr.command;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.permission.ManagePermissionChecker;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.qr.command.importqr.QrImportResponse;
import com.mryqr.core.qr.command.importqr.QrImporter;
import com.mryqr.core.qr.domain.AppedQr;
import com.mryqr.core.qr.domain.PlatedQr;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrDomainService;
import com.mryqr.core.qr.domain.QrFactory;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.common.exception.ErrorCode.AR_NOT_FOUND;
import static com.mryqr.core.common.exception.ErrorCode.CIRCULATION_OPTION_NOT_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_PLATE_NOT_IN_SAME_APP;
import static com.mryqr.core.common.exception.ErrorCode.GROUP_QR_NOT_SAME_APP;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_NOT_EXIT_FOR_BOUND;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_NOT_FOR_APP;
import static com.mryqr.core.common.exception.ErrorCode.QRS_SHOULD_IN_ONE_APP;
import static com.mryqr.core.common.utils.MapUtils.mapOf;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrCommandService {
    private final QrRepository qrRepository;
    private final QrDomainService qrDomainService;
    private final AppRepository appRepository;
    private final GroupRepository groupRepository;
    private final QrFactory qrFactory;
    private final PlateRepository plateRepository;
    private final ManagePermissionChecker managePermissionChecker;
    private final TenantRepository tenantRepository;
    private final MryRateLimiter mryRateLimiter;
    private final QrImporter qrImporter;

    @Transactional
    public CreateQrResponse createQr(CreateQrCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:Create", 20);
        String name = command.getName();
        String groupId = command.getGroupId();

        Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
        String appId = group.getAppId();
        App app = appRepository.cachedById(appId);

        managePermissionChecker.checkCanManageGroup(user, group, app);
        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateAddQr();
        packagesStatus.validateAddPlate();

        PlatedQr platedQr = qrFactory.createPlatedQr(name, group, app, user);
        qrRepository.save(platedQr.getQr());
        plateRepository.save(platedQr.getPlate());
        log.info("Created qr[{}] of group[{}] of app[{}].", platedQr.getQr().getId(), groupId, appId);

        return CreateQrResponse.builder()
                .qrId(platedQr.getQr().getId())
                .plateId(platedQr.getPlate().getId())
                .groupId(groupId)
                .appId(appId)
                .build();
    }

    @Transactional
    public CreateQrResponse createQrFromPlate(CreateQrFromPlateCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:CreateFromTemplate", 20);

        String plateId = command.getPlateId();
        String groupId = command.getGroupId();

        Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
        Plate plate = plateRepository.byIdAndCheckTenantShip(plateId, user);
        App app = appRepository.cachedById(group.getAppId());

        if (!group.getAppId().equals(plate.getAppId())) {
            throw new MryException(GROUP_PLATE_NOT_IN_SAME_APP, "分组和码牌不属于同一个应用。",
                    mapOf("groupId", groupId, "plateId", plateId));
        }

        managePermissionChecker.checkCanManageGroup(user, group, app);

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateAddQr();
        packagesStatus.validateAddPlate();

        QR qr = qrFactory.createQrFromPlate(command.getName(), group, plate, app, user);
        plate.bind(qr, user);
        qrRepository.save(qr);
        plateRepository.save(plate);

        log.info("Created qr[{}] from plate[{}] of group[{}] of app[{}].", qr.getId(), qr.getPlateId(), qr.getGroupId(), qr.getAppId());
        return CreateQrResponse.builder()
                .qrId(qr.getId())
                .plateId(plate.getId())
                .groupId(groupId)
                .appId(app.getId())
                .build();
    }

    public QrImportResponse importQrs(InputStream inputStream, String groupId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:Import", 1);

        Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
        managePermissionChecker.checkCanManageGroup(user, group);
        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateImportQr();

        App app = appRepository.cachedById(group.getAppId());
        int remainingCount = packagesStatus.validateImportQrs();

        QrImportResponse response = qrImporter.importQrs(inputStream, group, app, remainingCount, user);
        log.info("Imported {} qrs for group[{}].", response.getImportedCount(), groupId);
        return response;

    }

    @Transactional
    public void renameQr(String qrId, RenameQrCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:Rename", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        managePermissionChecker.checkCanManageQr(user, qr);

        String name = command.getName();
        if (Objects.equals(qr.getName(), name)) {
            return;
        }

        App app = appRepository.cachedById(qr.getAppId());
        qrDomainService.renameQr(qr, name, app, user);
        qrRepository.save(qr);
        log.info("Renamed qr[{}].", qrId);
    }

    @Transactional
    public void resetQrPlate(String qrId, ResetQrPlateCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:ResetPlate", 5);

        String newPlateId = command.getPlateId();

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        managePermissionChecker.checkCanManageQr(user, qr);

        if (qr.getPlateId().equals(newPlateId)) {
            return;
        }

        Plate newPlate;
        try {
            newPlate = plateRepository.byIdAndCheckTenantShip(newPlateId, user);
        } catch (MryException ex) {
            if (ex.getCode() == AR_NOT_FOUND) {
                throw new MryException(PLATE_NOT_EXIT_FOR_BOUND, "重置失败，码牌不存在。",
                        mapOf("qrId", qrId, "plateId", newPlateId));
            }
            throw ex;
        }

        if (!qr.getAppId().equals(newPlate.getAppId())) {
            throw new MryException(PLATE_NOT_FOR_APP,
                    "重置失败，码牌所在应用不一致。",
                    mapOf("plateId", newPlateId, "qrId", qrId, "appId", newPlate.getAppId()));
        }

        Plate oldPlate = plateRepository.byIdAndCheckTenantShip(qr.getPlateId(), user);

        qr.resetPlate(newPlateId, user);
        newPlate.bind(qr, user);
        oldPlate.unBind(user);

        plateRepository.save(newPlate);
        plateRepository.save(oldPlate);
        qrRepository.save(qr);
        log.info("Reset qr[{}] plate to plate[{}].", qrId, command.getPlateId());
    }

    @Transactional
    public void resetQrCirculationStatus(String qrId, ResetQrCirculationStatusCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:ResetCirculation", 5);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        managePermissionChecker.checkCanManageQr(user, qr);

        String circulationOptionId = command.getCirculationOptionId();
        App app = appRepository.cachedById(qr.getAppId());
        if (!app.hasCirculationStatus(circulationOptionId)) {
            throw new MryException(CIRCULATION_OPTION_NOT_EXISTS, "流转状态项不存在。", mapOf("optionId", circulationOptionId));
        }

        if (qr.updateCirculationStatus(circulationOptionId, user)) {
            qrRepository.save(qr);
            log.info("Updated qr[{}] circulation status to [{}].", qr.getId(), circulationOptionId);
        }
    }

    @Transactional
    public void deleteQrs(DeleteQrsCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:DeleteMulti", 5);

        Set<String> qrIds = command.getQrIds();
        List<QR> qrs = qrRepository.byIdsAllAndCheckTenantShip(qrIds, user);

        String appId = getAppIdAndCheck(qrs);
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        managePermissionChecker.checkCanManageApp(user, app);

        qrs.forEach(qr -> qr.onDelete(user));
        qrRepository.delete(qrs);
        log.info("Deleted qrs{}.", command.getQrIds());
    }

    @Transactional
    public void deleteQr(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:Delete", 5);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        managePermissionChecker.checkCanManageQr(user, qr);

        qr.onDelete(user);
        qrRepository.delete(qr);
        log.info("Deleted qr[{}].", qrId);
    }

    @Transactional
    public void changeQrsGroup(ChangeQrsGroupCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:ChangeGroup", 5);

        Set<String> qrIds = command.getQrIds();
        String toGroupId = command.getGroupId();

        List<QR> qrs = qrRepository.byIdsAllAndCheckTenantShip(qrIds, user);
        String appId = getAppIdAndCheck(qrs);
        App app = appRepository.cachedByIdAndCheckTenantShip(appId, user);
        Group group = groupRepository.byIdAndCheckTenantShip(toGroupId, user);

        managePermissionChecker.checkCanManageApp(user, app);
        if (!group.getAppId().equals(appId)) {
            throw new MryException(GROUP_QR_NOT_SAME_APP, "分组和实例不属于同一应用。",
                    mapOf("groupId", toGroupId, "appId", appId));
        }

        List<QR> toBeMovedQrs = qrs.stream().filter(qr -> !qr.getGroupId().equals(toGroupId)).collect(toImmutableList());
        if (toBeMovedQrs.isEmpty()) {
            return;
        }

        toBeMovedQrs.forEach(qr -> qr.changeGroup(toGroupId, user));
        qrRepository.save(toBeMovedQrs);
        log.info("Changed qrs{} to group[{}].", command.getQrIds(), command.getGroupId());
    }

    @Transactional
    public void updateQrBaseSetting(String qrId, UpdateQrBaseSettingCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:UpdateBaseSetting", 20);

        AppedQr appedQr = qrRepository.appedQrByIdAndCheckTenantShip(qrId, user);
        QR qr = appedQr.getQr();
        App app = appedQr.getApp();
        managePermissionChecker.checkCanManageQr(user, qr, app);

        qrDomainService.updateQrBaseSetting(qr,
                app,
                command.getName(),
                command.getDescription(),
                command.getHeaderImage(),
                command.getManualAttributeValues(),
                command.getGeolocation(),
                command.getCustomId(),
                user);

        qrRepository.houseKeepSave(qr, app, user);
        log.info("Updated base setting for qr[{}].", qrId);
    }

    @Transactional
    public void markAsTemplate(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:MarkTemplate", 5);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        managePermissionChecker.checkCanManageQr(user, qr);

        qr.markAsTemplate(user);
        qrRepository.save(qr);
        log.info("Marked qr[{}] as template.", qrId);
    }

    @Transactional
    public void unmarkAsTemplate(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:UnMarkTemplate", 5);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        managePermissionChecker.checkCanManageQr(user, qr);

        qr.unmarkAsTemplate(user);
        qrRepository.save(qr);
        log.info("Unmarked qr[{}] as template.", qrId);
    }

    @Transactional
    public void activateQr(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:Activate", 5);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        managePermissionChecker.checkCanManageQr(user, qr);

        qr.activate(user);
        qrRepository.save(qr);
        log.info("Activated qr[{}].", qrId);
    }

    @Transactional
    public void deactivateQr(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:Deactivate", 5);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        managePermissionChecker.checkCanManageQr(user, qr);

        qr.deactivate(user);
        qrRepository.save(qr);
        log.info("Deactivated qr[{}].", qrId);
    }

    private String getAppIdAndCheck(List<QR> qrs) {
        List<String> appIds = qrs.stream().map(QR::getAppId).distinct().collect(toImmutableList());
        if (appIds.size() >= 2) {
            throw new MryException(QRS_SHOULD_IN_ONE_APP, "批量操作只能针对单个应用。");
        }

        return appIds.get(0);
    }

}
