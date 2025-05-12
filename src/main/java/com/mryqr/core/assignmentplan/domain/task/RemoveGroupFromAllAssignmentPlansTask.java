package com.mryqr.core.assignmentplan.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.assignmentplan.domain.AssignmentPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveGroupFromAllAssignmentPlansTask implements RetryableTask {
    private final AssignmentPlanRepository assignmentPlanRepository;

    public void run(String groupId, String appId) {
        int count = assignmentPlanRepository.removeGroupFromAssignmentPlanExcludedGroups(groupId, appId);
        log.info("Removed excluded group[{}] from all {} assignment plans of app[{}].", groupId, count, appId);

        int removedCount = assignmentPlanRepository.removeGroupFromAssignmentPlanGroupOperators(groupId, appId);
        log.info("Removed group[{}] operators from all {} assignment plans of app[{}].", groupId, removedCount, appId);
    }

}
