package com.mryqr.core.plate.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.plate.domain.PlateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllPlatesUnderAppTask implements RetryableTask {
    private final PlateRepository plateRepository;

    public void run(String appId) {
        int count = plateRepository.removeAllPlatesUnderApp(appId);
        log.info("Removed all {} plates under app[{}].", count, appId);
    }

}
