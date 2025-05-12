package com.mryqr.core.tenant.job;

import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.tenant.domain.TenantRepository;
import com.mryqr.core.tenant.domain.task.CountStorageForTenantTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountStorageForAllTenantJob {
    private final CountStorageForTenantTask countStorageForTenantTask;
    private final TenantRepository tenantRepository;

    public void run() {
        log.info("Start count storages for all tenants.");

        List<String> allTenantIds = tenantRepository.allTenantIds();
        MryTaskRunner taskRunner = new MryTaskRunner();

        allTenantIds.forEach(tenantId -> {
            taskRunner.run(() -> countStorageForTenantTask.run(tenantId));
        });

        log.info("Finished count storages for all tenants.");
    }
}
