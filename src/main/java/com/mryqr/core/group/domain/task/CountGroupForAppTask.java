package com.mryqr.core.group.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountGroupForAppTask implements RetryableTask {
    private final TenantRepository tenantRepository;
    private final GroupRepository groupRepository;

    public void run(String appId, String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = groupRepository.countGroupForApp(appId);
            tenant.setGroupCountForApp(appId, count);
            tenantRepository.save(tenant);
            log.info("Counted {} groups for app[{}] for tenant[{}].", count, appId, tenant.getId());
        });
    }
}
