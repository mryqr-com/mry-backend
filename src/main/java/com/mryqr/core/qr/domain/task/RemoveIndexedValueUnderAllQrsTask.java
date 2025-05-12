package com.mryqr.core.qr.domain.task;

import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveIndexedValueUnderAllQrsTask implements RetryableTask {
    private final QrRepository qrRepository;

    public void run(IndexedField field, String appId) {
        this.run(Set.of(field), appId);
    }

    public void run(Set<IndexedField> fields, String appId) {
        int count = qrRepository.removeIndexedValueUnderAllQrs(fields, appId);
        log.info("Removed indexed values for fields{} for {} qrs of app[{}].", fields, count, appId);
    }

    public void run(IndexedField field, String attributeId, String appId) {
        int count = qrRepository.removeIndexedValueUnderAllQrs(field, attributeId, appId);
        log.info("Removed indexed values for field[{}] for attribute[{}] for {} qrs of app[{}].",
                field.name(), attributeId, count, appId);
    }
}
