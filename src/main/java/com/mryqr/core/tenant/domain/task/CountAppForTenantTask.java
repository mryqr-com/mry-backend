package com.mryqr.core.tenant.domain.task;

import com.mryqr.common.domain.task.RetryableTask;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.common.domain.user.User.NOUSER;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountAppForTenantTask implements RetryableTask {
    private final TenantRepository tenantRepository;
    private final AppRepository appRepository;

    public void run(String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = appRepository.countApp(tenantId);
            tenant.setAppCount(count, NOUSER);
            tenantRepository.save(tenant);
            log.info("Counted all {} apps for tenant[{}].", count, tenantId);
        });
    }
}
