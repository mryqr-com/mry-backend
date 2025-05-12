package com.mryqr.core.platebatch.domain.task;

import com.mryqr.core.common.domain.task.OnetimeTask;
import com.mryqr.core.platebatch.domain.PlateBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeltaCountUsedPlatesForPlateBatchTask implements OnetimeTask {
    private final PlateBatchRepository plateBatchRepository;

    public void delta(String plateBatchId, int delta) {
        int modifiedCount = plateBatchRepository.deltaCountUsedPlatesForPlateBatch(plateBatchId, delta);
        if (modifiedCount > 0) {
            log.info("Delta counted used plates for plate batch[{}] by {}.", plateBatchId, delta);
        }
    }

}
