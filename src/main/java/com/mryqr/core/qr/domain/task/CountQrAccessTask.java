package com.mryqr.core.qr.domain.task;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.task.OnetimeTask;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.sync.QrAttributeValueSyncer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_ACCESS_COUNT;
import static com.mryqr.core.common.domain.user.User.NOUSER;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountQrAccessTask implements OnetimeTask {
    private final QrAttributeValueSyncer qrAttributeValueSyncer;
    private final QrRepository qrRepository;

    public void run(QR qr, App app) {
        List<Attribute> accessCountAttributes = app.allAttributesOfTypes(INSTANCE_ACCESS_COUNT);
        try {
            if (isEmpty(accessCountAttributes)) {
                qrRepository.increaseAccessCount(qr);
            } else {
                qr.access();
                qrAttributeValueSyncer.sync(qr, app, accessCountAttributes, NOUSER);
            }
        } catch (Throwable t) {
            log.warn("Failed to count access for qr[{}].", qr.getId(), t);
        }
    }

}
