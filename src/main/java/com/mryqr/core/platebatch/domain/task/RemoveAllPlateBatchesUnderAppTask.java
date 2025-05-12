package com.mryqr.core.platebatch.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.platebatch.domain.PlateBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllPlateBatchesUnderAppTask implements RetryableTask {
    private final PlateBatchRepository plateBatchRepository;

    public void run(String appId) {
        int count = plateBatchRepository.removeAllPlateBatchUnderApp(appId);
        log.info("Removed all {} plate batches under app[{}].", count, appId);
    }
}
