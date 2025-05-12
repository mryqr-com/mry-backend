package com.mryqr.core.assignment.domain.task;

import com.mryqr.core.assignment.domain.AssignmentRepository;
import com.mryqr.core.common.domain.task.RepeatableTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllAssignmentsUnderGroupTask implements RepeatableTask {
    private final AssignmentRepository assignmentRepository;

    public void run(String groupId) {
        int count = assignmentRepository.removeAssignmentsUnderGroup(groupId);
        log.info("Removed all {} assignments under group[{}].", count, groupId);
    }
}
