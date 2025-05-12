package com.mryqr.core.qr.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.plate.domain.Plate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.exception.ErrorCode.GROUP_NOT_ACTIVE;
import static com.mryqr.core.common.exception.ErrorCode.PLATE_ALREADY_BOUND;
import static com.mryqr.core.common.exception.ErrorCode.QR_WITH_CUSTOM_ID_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.QR_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class QrFactory {
    private final QrRepository qrRepository;

    public PlatedQr createPlatedQr(String name, Group group, App app, User user) {
        return this.createPlatedQr(name, group, app, null, user);
    }

    public PlatedQr createPlatedQr(String name, Group group, App app, String customId, User user) {
        checkNameDuplication(name, app);
        checkCustomIdDuplication(customId, app);
        checkGroupActive(group, app);

        QR qr = new QR(name, group.getId(), app, customId, user);
        Plate plate = new Plate(qr, user);
        return new PlatedQr(qr, plate);
    }

    public PlatedQr createPlatedQr(String name, String plateId, Group group, App app, User user) {
        checkNameDuplication(name, app);
        checkGroupActive(group, app);

        QR qr = new QR(name, plateId, group.getId(), app, user);
        Plate plate = new Plate(qr, user);
        return new PlatedQr(qr, plate);
    }

    public PlatedQr createImportedPlatedQr(String name, Group group, App app, String customId, User user) {
        checkGroupActive(group, app);

        QR qr = new QR(name, group.getId(), app, customId, user);
        Plate plate = new Plate(qr, user);
        return new PlatedQr(qr, plate);
    }

    public PlatedQr createPlatedQrFromTemplate(QR templateQr, App app, User user) {
        QR qr = new QR(templateQr, app, user);//必须在前面，否则plate拿不到ID
        Plate plate = new Plate(qr, user);
        return new PlatedQr(qr, plate);
    }

    public QR createQrFromPlate(String name, Group group, Plate plate, App app, User user) {
        checkGroupActive(group, app);

        if (plate.isBound()) {
            throw new MryException(PLATE_ALREADY_BOUND, "码牌已经绑定，无法创建" + app.instanceDesignation() + "。",
                    mapOf("plateId", plate.getId()));
        }
        checkNameDuplication(name, app);
        return new QR(name, group.getId(), plate, app, user);
    }

    private void checkGroupActive(Group group, App app) {
        if (!group.isActive()) {
            throw new MryException(GROUP_NOT_ACTIVE, "创建失败，" + app.groupDesignation() + "已被禁用。");
        }
    }

    private void checkNameDuplication(String name, App app) {
        if (app.notAllowDuplicateInstanceName() && qrRepository.existsByName(name, app.getId())) {
            throw new MryException(QR_WITH_NAME_ALREADY_EXISTS, "创建失败，名称已被占用。",
                    mapOf("name", name, "appId", app.getId()));
        }
    }

    private void checkCustomIdDuplication(String customId, App app) {
        if (isNotBlank(customId) && qrRepository.existsByCustomId(customId, app.getId())) {
            throw new MryException(QR_WITH_CUSTOM_ID_ALREADY_EXISTS, app.customIdDesignation() + "已被占用。",
                    mapOf("customId", customId, "appId", app.getId()));
        }
    }
}
