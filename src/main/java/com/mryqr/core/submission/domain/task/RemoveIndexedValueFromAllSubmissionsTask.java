package com.mryqr.core.submission.domain.task;

import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.core.submission.domain.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveIndexedValueFromAllSubmissionsTask {
    private final SubmissionRepository submissionRepository;

    public void run(IndexedField field, String pageId, String appId) {
        int count = submissionRepository.removeIndexedValueFromAllSubmissions(field, pageId, appId);
        log.info("Removed indexed values for field[{}] for {} submissions of page[{}] of app[{}].",
                field.name(), count, pageId, appId);
    }

    public void run(IndexedField field, String controlId, String pageId, String appId) {
        int count = submissionRepository.removeIndexedValueFromAllSubmissions(field, controlId, pageId, appId);
        log.info("Removed indexed values for field[{}] for control[{}] for {} submissions of page[{}] of app[{}].",
                field.name(), controlId, count, pageId, appId);
    }
}
