package com.mryqr.core.submission.domain.task;

import com.mryqr.core.common.domain.task.OnetimeTask;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeltaCountSubmissionForAppTask implements OnetimeTask {
    private final TenantRepository tenantRepository;

    public void delta(String appId, String tenantId, int delta) {
        int modifiedCount = tenantRepository.deltaCountSubmissionForApp(appId, tenantId, delta);
        if (modifiedCount > 0) {
            log.info("Delta counted submissions for app[{}] by {}.", appId, delta);
        }
    }

}
