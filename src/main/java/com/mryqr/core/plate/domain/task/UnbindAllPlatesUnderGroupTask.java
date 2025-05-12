package com.mryqr.core.plate.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.plate.domain.PlateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnbindAllPlatesUnderGroupTask implements RetryableTask {
    private final PlateRepository plateRepository;

    public void run(String groupId) {
        int count = plateRepository.unbindAllPlatesUnderGroup(groupId);
        log.info("Unbound {} plates from group[{}].", count, groupId);
    }

}
