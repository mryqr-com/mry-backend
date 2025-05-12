package com.mryqr.core.tenant.domain.task;

import com.mryqr.core.common.domain.task.OnetimeTask;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeltaCountMemberForTenantTask implements OnetimeTask {
    private final TenantRepository tenantRepository;

    public void delta(String tenantId, int delta) {
        int modifiedCount = tenantRepository.deltaCountMember(tenantId, delta);
        if (modifiedCount > 0) {
            log.info("Delta counted members for tenant[{}] by {}.", tenantId, delta);
        }
    }
}
