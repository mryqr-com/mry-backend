package com.mryqr.core.submission.domain.task;

import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveSubmissionIndexedOptionForAppTask implements RepeatableTask {
    private final AppRepository appRepository;
    private final SubmissionRepository submissionRepository;

    public void run(String appId, String pageId, String controlId, String optionId) {
        appRepository.byIdOptional(appId).ifPresent(app -> {
            app.indexedFieldForControlOptional(pageId, controlId).ifPresent(indexedField -> {
                int count = submissionRepository.removeIndexedOptionFromAllSubmissions(optionId, indexedField, pageId, appId);
                log.info("Removed option[{}] from {} submissions of control[{}] of page[{}] of app[{}].",
                        optionId, count, controlId, pageId, app.getId());
            });
        });
    }

}
