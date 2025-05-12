package com.mryqr.core.submission.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllSubmissionsForAppTask implements RepeatableTask {
    private final SubmissionRepository submissionRepository;

    public void run(String appId) {
        int count = submissionRepository.removeAllSubmissionForApp(appId);
        log.info("Removed all {} submissions under app[{}].", count, appId);
    }

}
