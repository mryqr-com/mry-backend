package com.mryqr.core.app.domain.task;

import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.TenantCachedApp;
import com.mryqr.core.common.domain.task.RepeatableTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveManagerFromAllAppsTask implements RepeatableTask {
    private final AppRepository appRepository;

    public void run(String memberId, String tenantId) {
        List<TenantCachedApp> allApps = appRepository.cachedTenantAllApps(tenantId);
        allApps.stream().filter(app -> app.getManagers().contains(memberId))
                .forEach(app -> appRepository.evictAppCache(app.getId()));

        if (allApps.stream().anyMatch(group -> group.getManagers().contains(memberId))) {
            appRepository.evictTenantAppsCache(tenantId);
        }

        int modifiedCount = appRepository.removeManagerFromAllApps(memberId, tenantId);
        if (modifiedCount > 0) {
            log.info("Removed manager[{}] from all {} apps of tenant[{}].", memberId, modifiedCount, tenantId);
        }
    }
}
