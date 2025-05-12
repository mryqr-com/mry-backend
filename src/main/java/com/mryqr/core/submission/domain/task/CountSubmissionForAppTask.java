package com.mryqr.core.submission.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountSubmissionForAppTask implements RetryableTask {
    private final TenantRepository tenantRepository;
    private final SubmissionRepository submissionRepository;

    public void run(String appId, String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = submissionRepository.countSubmissionForApp(appId);
            tenant.setSubmissionCountForApp(appId, count);
            tenantRepository.save(tenant);
            log.debug("Counted {} submissions for app[{}] of tenant[{}].", count, appId, tenantId);
        });
    }

}
