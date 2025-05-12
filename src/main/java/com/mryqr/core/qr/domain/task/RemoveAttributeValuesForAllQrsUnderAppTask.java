package com.mryqr.core.qr.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAttributeValuesForAllQrsUnderAppTask implements RetryableTask {
    private final QrRepository qrRepository;

    public void run(Set<String> attributeIds, String appId) {
        int count = qrRepository.removeAttributeValuesUnderAllQrs(attributeIds, appId);
        log.info("Removed attributes{} values from all {} qrs of app[{}].", attributeIds, count, appId);
    }
}
