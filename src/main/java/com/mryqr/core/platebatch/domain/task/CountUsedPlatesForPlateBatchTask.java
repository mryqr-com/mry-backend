package com.mryqr.core.platebatch.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.plate.domain.PlateRepository;
import com.mryqr.core.platebatch.domain.PlateBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountUsedPlatesForPlateBatchTask implements RetryableTask {
    private final PlateBatchRepository plateBatchRepository;
    private final PlateRepository plateRepository;

    public void run(String plateBatchId) {
        plateBatchRepository.byIdOptional(plateBatchId).ifPresent(plateBatch -> {
            int count = plateRepository.countUsedPlatesForPlateBatch(plateBatchId);
            plateBatch.updateUsedCount(count);
            plateBatchRepository.save(plateBatch);
            log.info("Counted {} used plates for plate batch[{}].", count, plateBatchId);
        });
    }

}
