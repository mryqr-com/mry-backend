package com.mryqr.core.qr.domain.task;


import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeType;
import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.sync.QrAttributeValueSyncer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.core.common.utils.CommonUtils.requireNonBlank;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncAttributeValuesForQrTask implements RepeatableTask {
    private final AppRepository appRepository;
    private final QrRepository qrRepository;
    private final QrAttributeValueSyncer qrAttributeValueSyncer;

    public void run(String qrId) {
        qrRepository.byIdOptional(qrId).ifPresent(qr -> appRepository.cachedByIdOptional(qr.getAppId()).ifPresent(app -> {
            List<Attribute> attributes = app.allCalculatedAttributes();
            if (isEmpty(attributes)) {
                return;
            }

            qrAttributeValueSyncer.sync(qr, app, attributes, NOUSER);
            log.info("Synced all {} calculated attributes{} for qr[{}].",
                    attributes.size(), attributes.stream().map(Attribute::getId).collect(toImmutableList()), qr.getId());
        }));
    }

    public void run(String qrId, AttributeType... attributeTypes) {
        requireNonBlank(qrId, "QR ID must not be blank.");

        qrRepository.byIdOptional(qrId).ifPresent(qr -> appRepository.cachedByIdOptional(qr.getAppId()).ifPresent(app -> {
            List<Attribute> attributes = app.allAttributesOfTypes(attributeTypes);
            if (isEmpty(attributes)) {
                return;
            }

            qrAttributeValueSyncer.sync(qr, app, attributes, NOUSER);
            log.info("Synced {} attributes{} for qr[{}].",
                    attributes.size(), attributes.stream().map(Attribute::getId).collect(toImmutableList()), qr.getId());
        }));
    }
}
