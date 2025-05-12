package com.mryqr.core.qr.domain.task;

import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveIndexedOptionUnderAllQrsTask implements RepeatableTask {
    private final AppRepository appRepository;
    private final QrRepository qrRepository;

    public void run(String appId, String controlId, String optionId) {
        appRepository.byIdOptional(appId).ifPresent(app -> {
            List<Attribute> attributes = app.controlReferencedAttributes(controlId);
            attributes.forEach(attribute -> {
                app.indexedFieldForAttributeOptional(attribute.getId()).ifPresent(field -> {
                    int count = qrRepository.removeIndexedOptionUnderAllQrs(optionId, field, appId);
                    log.info("Removed option[{}] from attribute[{}] for {} qrs of app[{}].",
                            optionId, attribute.getId(), count, app.getId());
                });
            });
        });
    }

}
