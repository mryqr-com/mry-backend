package com.mryqr.core.assignmentplan.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.assignmentplan.domain.AssignmentPlan;
import com.mryqr.core.assignmentplan.domain.AssignmentPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.common.domain.user.User.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveOperatorFromAllAssignmentPlansTask implements RetryableTask {
    private final AssignmentPlanRepository assignmentPlanRepository;

    public void run(String memberId, String tenantId) {
        List<AssignmentPlan> assignmentPlans = assignmentPlanRepository.allAssignmentPlansOf(tenantId);
        assignmentPlans.forEach(assignmentPlan -> {
            if (assignmentPlan.containsOperator(memberId)) {
                assignmentPlan.removeOperator(memberId, NOUSER);
                assignmentPlanRepository.save(assignmentPlan);
            }
        });

        log.info("Removed member[{}] from all assignment plans of tenant[{}].", memberId, tenantId);
    }
}
