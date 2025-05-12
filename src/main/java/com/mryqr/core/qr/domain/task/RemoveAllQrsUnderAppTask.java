package com.mryqr.core.qr.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllQrsUnderAppTask implements RetryableTask {
    private final QrRepository qrRepository;

    public void run(String appId) {
        int count = qrRepository.removeAllQrsUnderApp(appId);
        log.info("Removed all {} qrs under app[{}].", count, appId);
    }

}
