package com.mryqr.integration.qr.command;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.plate.domain.Plate;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.qr.domain.*;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.Objects;

import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationQrCommandService {
    private final QrRepository qrRepository;
    private final AppRepository appRepository;
    private final GroupRepository groupRepository;
    private final QrFactory qrFactory;
    private final PlateRepository plateRepository;
    private final TenantRepository tenantRepository;
    private final MryRateLimiter mryRateLimiter;
    private final QrDomainService qrDomainService;

    @Transactional
    public IntegrationCreateQrResponse createQrSimple(IntegrationCreateQrSimpleCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:SimpleCreate", 10);

        String name = command.getName();
        String groupId = command.getGroupId();
        String customId = command.getCustomId();

        Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
        String appId = group.getAppId();

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateAddPlate();
        packagesStatus.validateAddQr();

        App app = appRepository.cachedById(appId);
        PlatedQr platedQr = qrFactory.createPlatedQr(name, group, app, customId, user);
        QR qr = platedQr.getQr();
        Plate plate = platedQr.getPlate();
        qrRepository.save(qr);
        plateRepository.save(plate);

        log.info("Integration simple created qr[{}] under group[{}] of app[{}].", qr.getId(), groupId, appId);
        return IntegrationCreateQrResponse.builder()
                .qrId(qr.getId())
                .plateId(plate.getId())
                .groupId(groupId)
                .appId(appId)
                .build();
    }

    @Transactional
    public IntegrationCreateQrResponse createQrAdvanced(IntegrationCreateQrAdvancedCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:AdvanceCreate", 10);

        String groupId = command.getGroupId();
        Group group = groupRepository.cachedByIdAndCheckTenantShip(groupId, user);
        String appId = group.getAppId();

        PackagesStatus packagesStatus = tenantRepository.packagesStatusOf(user.getTenantId());
        packagesStatus.validateAddPlate();
        packagesStatus.validateAddQr();

        App app = appRepository.cachedById(appId);
        String name = command.getName();
        String customId = command.getCustomId();
        PlatedQr platedQr = qrFactory.createPlatedQr(name, group, app, customId, user);
        QR qr = platedQr.getQr();
        Plate plate = platedQr.getPlate();

        qrDomainService.updateQrBaseSetting(qr,
                app,
                name,
                command.getDescription(),
                toUploadedFile(command.getHeaderImageUrl()),
                command.getDirectAttributeValues(),
                command.getGeolocation(),
                customId,
                user);

        qrRepository.houseKeepSave(qr, app, user);
        plateRepository.save(plate);

        log.info("Integration advanced created qr[{}] under group[{}] of app[{}].", qr.getId(), groupId, appId);
        return IntegrationCreateQrResponse.builder()
                .qrId(qr.getId())
                .plateId(plate.getId())
                .groupId(groupId)
                .appId(appId)
                .build();
    }

    @Transactional
    public void deleteQr(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Delete", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        qr.onDelete(user);
        qrRepository.delete(qr);
        log.info("Integration deleted qr[{}].", qrId);
    }

    @Transactional
    public void deleteQrByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:Delete", 10);

        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        qr.onDelete(user);
        qrRepository.delete(qr);
        log.info("Integration deleted qr[appId={},customId={}].", appId, customId);
    }

    @Transactional
    public void activateQr(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Activate", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        qr.activate(user);
        qrRepository.save(qr);
        log.info("Integration activated QR[{}].", qrId);
    }

    @Transactional
    public void activateQrByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:Activate", 10);

        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        qr.activate(user);
        qrRepository.save(qr);
        log.info("Integration activated qr[appId={},customId={}].", appId, customId);
    }

    @Transactional
    public void deactivateQr(String qrId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Deactivate", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        qr.deactivate(user);
        qrRepository.save(qr);
        log.info("Integration deactivated QR[{}].", qrId);
    }

    @Transactional
    public void deactivateQrByCustomId(String appId, String customId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:Deactivate", 10);

        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        qr.deactivate(user);
        qrRepository.save(qr);
        log.info("Integration deactivated qr[appId={},customId={}].", appId, customId);
    }

    @Transactional
    public void renameQr(String qrId, IntegrationRenameQrCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Rename", 10);

        String newName = command.getName();
        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        if (Objects.equals(qr.getName(), newName)) {
            return;
        }

        App app = appRepository.cachedById(qr.getAppId());
        qrDomainService.renameQr(qr, newName, app, user);
        qrRepository.save(qr);
        log.info("Integration renamed qr[{}].", qrId);
    }

    @Transactional
    public void renameQrByCustomId(String appId, String customId, IntegrationRenameQrCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:Rename", 10);

        String newName = command.getName();
        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        if (Objects.equals(qr.getName(), newName)) {
            return;
        }

        App app = appRepository.cachedById(qr.getAppId());
        qrDomainService.renameQr(qr, newName, app, user);
        qrRepository.save(qr);
        log.info("Integration renamed qr[appId={},customId={}].", appId, customId);
    }

    @Transactional
    public void updateQrBaseSetting(String qrId, IntegrationUpdateQrBaseSettingCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:UpdateBaseSetting", 10);

        AppedQr appedQr = qrRepository.appedQrByIdAndCheckTenantShip(qrId, user);
        QR qr = appedQr.getQr();
        App app = appedQr.getApp();

        qrDomainService.updateQrBaseSetting(qr,
                app,
                command.getName(),
                command.getDescription(),
                toUploadedFile(command.getHeaderImageUrl()),
                command.getDirectAttributeValues(),
                command.getGeolocation(),
                command.getCustomId(),
                user);

        qrRepository.houseKeepSave(qr, app, user);
        log.info("Integration updated base setting for qr[{}].", qrId);
    }

    private UploadedFile toUploadedFile(String headerImageUrl) {
        return isNotBlank(headerImageUrl) ?
                UploadedFile.builder()
                        .id(newShortUuid())
                        .name(fileNameOf(headerImageUrl))
                        .fileUrl(headerImageUrl)
                        .type(fileTypeOf(headerImageUrl))
                        .size(100)
                        .build()
                : null;
    }

    private String fileTypeOf(String headerImageUrl) {
        return headerImageUrl.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
    }

    private String fileNameOf(String headerImageUrl) {
        try {
            return getName(new URL(headerImageUrl).getPath());
        } catch (Throwable t) {
            return "未命名";
        }
    }

    @Transactional
    public void updateQrBaseSettingByCustomId(String appId, String customId, IntegrationUpdateQrBaseSettingCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:UpdateBaseSetting", 10);

        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        App app = appRepository.cachedById(appId);

        qrDomainService.updateQrBaseSetting(qr,
                app,
                command.getName(),
                command.getDescription(),
                toUploadedFile(command.getHeaderImageUrl()),
                command.getDirectAttributeValues(),
                command.getGeolocation(),
                command.getCustomId(),
                user);

        qrRepository.houseKeepSave(qr, app, user);
        log.info("Integration updated base setting for qr[appId={},customId={}].", appId, customId);
    }

    @Transactional
    public void updateQrCustomId(String qrId, IntegrationUpdateQrCustomIdCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:UpdateCustomId", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        App app = appRepository.cachedById(qr.getAppId());
        qrDomainService.updateQrCustomId(qr, command.getCustomId(), app, user);
        qrRepository.save(qr);
        log.info("Integration updated custom ID for qr[{}].", qrId);
    }

    @Transactional
    public void updateQrDescription(String qrId, IntegrationUpdateQrDescriptionCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:UpdateDescription", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        qr.updateDescription(command.getDescription(), user);
        qrRepository.save(qr);
        log.info("Integration updated description for qr[{}].", qrId);
    }

    @Transactional
    public void updateQrDescriptionByCustomId(String appId, String customId, IntegrationUpdateQrDescriptionCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:UpdateDescription", 10);

        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        qr.updateDescription(command.getDescription(), user);
        qrRepository.save(qr);
        log.info("Integration updated description for qr[appId={},customId={}].", appId, customId);
    }

    @Transactional
    public void updateQrHeaderImage(String qrId, IntegrationUpdateQrHeaderImageCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:UpdateHeaderImage", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        qr.updateHeaderImage(toUploadedFile(command.getHeaderImageUrl()), user);
        qrRepository.save(qr);
        log.info("Integration updated header image for qr[{}].", qrId);
    }

    @Transactional
    public void updateQrHeaderImageByCustomId(String appId, String customId, IntegrationUpdateQrHeaderImageCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:UpdateHeaderImage", 10);

        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        qr.updateHeaderImage(toUploadedFile(command.getHeaderImageUrl()), user);
        qrRepository.save(qr);
        log.info("Integration updated header image for qr[appId={},customId={}].", appId, customId);
    }

    @Transactional
    public void updateQrDirectAttributes(String qrId, IntegrationUpdateQrDirectAttributesCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:UpdateDirectAttributes", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        App app = appRepository.cachedById(qr.getAppId());
        qrDomainService.updateQrDirectAttributes(qr, app, command.getDirectAttributeValues(), user);
        qrRepository.houseKeepSave(qr, app, user);
        log.info("Integration updated direct attributes for qr[{}].", qrId);
    }

    @Transactional
    public void updateQrDirectAttributesByCustomId(String appId, String customId, IntegrationUpdateQrDirectAttributesCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:UpdateDirectAttributes", 10);

        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        App app = appRepository.cachedById(qr.getAppId());
        qrDomainService.updateQrDirectAttributes(qr, app, command.getDirectAttributeValues(), user);
        qrRepository.houseKeepSave(qr, app, user);
        log.info("Integration updated direct attributes for qr[appId={},customId={}].", appId, customId);
    }

    @Transactional
    public void updateQrGeolocation(String qrId, IntegrationUpdateQrGeolocationCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:UpdateGeolocation", 10);

        QR qr = qrRepository.byIdAndCheckTenantShip(qrId, user);
        qr.updateGeolocation(command.getGeolocation(), user);
        qrRepository.save(qr);
        log.info("Integration updated geolocation for qr[{}].", qrId);
    }

    @Transactional
    public void updateQrGeolocationByCustomId(String appId, String customId, IntegrationUpdateQrGeolocationCommand command, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:QR:Custom:UpdateGeolocation", 10);

        QR qr = qrRepository.byCustomIdAndCheckTenantShip(appId, customId, user);
        qr.updateGeolocation(command.getGeolocation(), user);
        qrRepository.save(qr);
        log.info("Integration updated geolocation for qr[appId={},customId={}].", appId, customId);
    }
}
