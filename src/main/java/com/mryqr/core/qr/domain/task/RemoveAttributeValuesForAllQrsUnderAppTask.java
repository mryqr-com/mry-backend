package com.mryqr.core.qr.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAttributeValuesForAllQrsUnderAppTask implements RepeatableTask {
    private final QrRepository qrRepository;

    public void run(Set<String> attributeIds, String appId) {
        int count = qrRepository.removeAttributeValuesUnderAllQrs(attributeIds, appId);
        log.info("Removed attributes{} values from all {} qrs of app[{}].", attributeIds, count, appId);
    }
}
