package com.mryqr.core.appmanual.domain.task;

import com.mryqr.core.appmanual.domain.AppManualRepository;
import com.mryqr.core.common.domain.task.RepeatableTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveManualForAppTask implements RepeatableTask {
    private final AppManualRepository appManualRepository;

    public void run(String appId) {
        int removedCount = appManualRepository.removeAppManual(appId);
        if (removedCount > 0) {
            log.info("Removed app manual for app[{}].", appId);
        }
    }

}
