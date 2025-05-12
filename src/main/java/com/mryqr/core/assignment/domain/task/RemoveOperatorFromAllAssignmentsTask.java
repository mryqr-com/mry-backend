package com.mryqr.core.assignment.domain.task;

import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.common.domain.task.RepeatableTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveOperatorFromAllAssignmentsTask implements RepeatableTask {
    private final AssignmentRepository assignmentRepository;

    public void run(String memberId, String tenantId) {
        int count = assignmentRepository.removeOperatorFromAllAssignments(memberId, tenantId);
        log.info("Removed operators[{}] from all {} assignments of tenant[{}].", memberId, count, tenantId);
    }
}
