package com.mryqr.core.qr.domain.task;

import com.mryqr.common.domain.task.NonRetryableTask;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.sync.QrAttributeValueSyncer;
import com.mryqr.management.platform.domain.PlatformRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_ACCESS_COUNT;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordQrAccessTask implements NonRetryableTask {
    private final QrAttributeValueSyncer qrAttributeValueSyncer;
    private final QrRepository qrRepository;
    private final PlatformRepository platformRepository;

    public void run(QR qr, App app, boolean isMobileRequest) {
        try {
            recordQrAccessCount(qr, app);
        } catch (Throwable t) {
            log.warn("Failed to count access for qr[{}].", qr.getId(), t);
        }

        try {
            recordPlatformAccessCount(app, isMobileRequest);
        } catch (Throwable t) {
            log.warn("Failed to count platform access.", t);
        }
    }

    private void recordQrAccessCount(QR qr, App app) {
        List<Attribute> accessCountAttributes = app.allAttributesOfTypes(INSTANCE_ACCESS_COUNT);

        if (isEmpty(accessCountAttributes)) {
            qrRepository.increaseAccessCount(qr);
        } else {
            qr.access();
            qrAttributeValueSyncer.sync(qr, app, accessCountAttributes, NO_USER);
        }
    }

    private void recordPlatformAccessCount(App app, boolean isMobileRequest) {
        if (app.getTenantId().equals(MRY_MANAGE_TENANT_ID)) {
            return;
        }

        if (isMobileRequest) {
            this.platformRepository.increaseMobileAccessCount();
        } else {
            this.platformRepository.increaseNonMobileAccessCount();
        }
    }

}
