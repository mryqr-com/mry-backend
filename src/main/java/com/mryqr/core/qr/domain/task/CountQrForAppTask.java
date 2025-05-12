package com.mryqr.core.qr.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CountQrForAppTask implements RepeatableTask {
    private final TenantRepository tenantRepository;
    private final QrRepository qrRepository;

    public void run(String appId, String tenantId) {
        tenantRepository.byIdOptional(tenantId).ifPresent(tenant -> {
            int count = qrRepository.countQrUnderApp(appId);
            tenant.setQrCountForApp(appId, count);
            tenantRepository.save(tenant);
            log.info("Counted {} qrs for app[{}] of tenant[{}].", count, appId, tenant.getId());
        });
    }
}
