package com.mryqr.core.submission.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAnswersForControlsFromAllSubmissionsTask implements RetryableTask {
    private final SubmissionRepository submissionRepository;

    public void run(Set<String> controlIds, String appId) {
        int count = submissionRepository.removeControlAnswersFromAllSubmissions(controlIds, appId);
        log.info("Deleted all answers for controls[{}] from all {} submissions of app[{}].", controlIds, count, appId);
    }

}
