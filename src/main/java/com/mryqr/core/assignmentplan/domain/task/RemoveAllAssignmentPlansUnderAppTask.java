package com.mryqr.core.assignmentplan.domain.task;

import com.mryqr.core.assignmentplan.domain.AssignmentPlanRepository;
import com.mryqr.core.common.domain.task.RepeatableTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllAssignmentPlansUnderAppTask implements RepeatableTask {
    private final AssignmentPlanRepository assignmentPlanRepository;

    public void run(String appId) {
        int count = assignmentPlanRepository.removeAllAssignmentPlansUnderApp(appId);
        log.info("Removed all {} assignment plans under app[{}].", count, appId);
    }
}
