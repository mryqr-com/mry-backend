package com.mryqr.core.assignmentplan.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.assignmentplan.domain.AssignmentPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllAssignmentPlansForPageTask implements RetryableTask {
    private final AssignmentPlanRepository assignmentPlanRepository;

    public void run(String pageId, String appId) {
        int count = assignmentPlanRepository.removeAllAssignmentPlansUnderPage(pageId, appId);
        log.info("Removed all {} assignment plans for page[{}] under app[{}].", count, pageId, appId);
    }
}
