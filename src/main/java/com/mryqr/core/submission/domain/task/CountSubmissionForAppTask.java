package com.mryqr.core.submission.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.submission.domain.SubmissionRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountSubmissionForAppTask implements RepeatableTask {
    private final TenantRepository tenantRepository;
    private final SubmissionRepository submissionRepository;

    public void run(String appId, String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = submissionRepository.countSubmissionForApp(appId);
            tenant.setSubmissionCountForApp(appId, count);
            tenantRepository.save(tenant);
            log.info("Counted {} submissions for app[{}] of tenant[{}].", count, appId, tenantId);
        });
    }

}
