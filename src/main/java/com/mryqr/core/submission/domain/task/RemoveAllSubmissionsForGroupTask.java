package com.mryqr.core.submission.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllSubmissionsForGroupTask implements RetryableTask {
    private final SubmissionRepository submissionRepository;

    public void run(String groupId) {
        int count = submissionRepository.removeAllSubmissionForGroup(groupId);
        log.info("Removed all {} submissions under group[{}].", count, groupId);
    }

}
