package com.mryqr.core.qr.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.sync.QrAttributeValueSyncer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncSubmissionAwareAttributeValuesForQrTask implements RetryableTask {
    private final QrRepository qrRepository;
    private final AppRepository appRepository;
    private final QrAttributeValueSyncer qrAttributeValueSyncer;

    public void run(String qrId) {
        qrRepository.byIdOptional(qrId).ifPresent(qr -> appRepository.cachedByIdOptional(qr.getAppId()).ifPresent(app -> {
            List<Attribute> attributes = app.allSubmissionAwareAttributes();

            if (isEmpty(attributes)) {
                return;
            }

            qrAttributeValueSyncer.sync(qr, app, attributes, NOUSER);
            log.debug("Synced all {} submission aware attributes for qr[{}].", attributes.size(), qrId);
        }));
    }

    public void run(String qrId, String pageId) {
        requireNonBlank(qrId, "QR ID must not be blank.");
        requireNonBlank(pageId, "Page ID must not be blank.");

        qrRepository.byIdOptional(qrId).ifPresent(qr -> appRepository.cachedByIdOptional(qr.getAppId()).ifPresent(app -> {
            List<Attribute> attributes = app.allPageSubmissionAwareAttributes(pageId);

            if (isEmpty(attributes)) {
                return;
            }

            qrAttributeValueSyncer.sync(qr, app, attributes, NOUSER);
            log.debug("Synced all {} submission aware attributes for qr[{}] with page[{}].", attributes.size(), qrId, pageId);
        }));
    }

}
