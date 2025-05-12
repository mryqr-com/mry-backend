package com.mryqr.core.apptemplate.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.DoubleAttributeValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.google.common.collect.ImmutableMap.of;
import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.management.apptemplate.MryAppTemplateManageApp.APPLIED_COUNT_ATTRIBUTE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountAppTemplateAppliedTask implements RetryableTask {
    private final QrRepository qrRepository;
    private final AppRepository appRepository;

    public void run(String appTemplateId) {
        qrRepository.byIdOptional(appTemplateId).ifPresent(qr -> {
            int count = appRepository.appTemplateAppliedCountFor(appTemplateId);
            App app = appRepository.cachedById(qr.getAppId());
            app.attributeByIdOptional(APPLIED_COUNT_ATTRIBUTE_ID).ifPresent(attribute -> {
                DoubleAttributeValue attributeValue = new DoubleAttributeValue(attribute, (double) count);
                qr.putAttributeValues(of(attribute.getId(), attributeValue), NO_USER);
                qrRepository.houseKeepSave(qr, app, NO_USER);
                log.info("Counted {} usages for app template[{}].", count, appTemplateId);
            });
        });
    }
}
