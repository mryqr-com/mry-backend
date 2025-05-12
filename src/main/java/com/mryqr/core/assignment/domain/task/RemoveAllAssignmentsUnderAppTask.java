package com.mryqr.core.assignment.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.assignment.domain.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllAssignmentsUnderAppTask implements RetryableTask {
    private final AssignmentRepository assignmentRepository;

    public void run(String appId) {
        int count = assignmentRepository.removeAssignmentsUnderApp(appId);
        log.info("Removed all {} assignments under app[{}].", count, appId);
    }
}
