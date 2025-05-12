package com.mryqr.core.assignment.domain.task;

import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.common.domain.task.RepeatableTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllAssignmentsUnderAssignmentPlanTask implements RepeatableTask {
    private final AssignmentRepository assignmentRepository;

    public void run(String assignmentPlanId) {
        int count = assignmentRepository.removeAssignmentsUnderAssignmentPlan(assignmentPlanId);
        log.info("Removed all {} assignments under assignment plan[{}].", count, assignmentPlanId);
    }
}
