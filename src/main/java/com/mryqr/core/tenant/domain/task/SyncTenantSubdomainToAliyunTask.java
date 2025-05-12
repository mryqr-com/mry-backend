package com.mryqr.core.tenant.domain.task;

import com.mryqr.common.dns.MryDnsService;
import com.mryqr.common.domain.task.NonRetryableTask;
import com.mryqr.common.properties.AliyunProperties;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncTenantSubdomainToAliyunTask implements NonRetryableTask {
    private final MryDnsService mryDnsService;
    private final TenantRepository tenantRepository;
    private final AliyunProperties aliyunProperties;

    public void run(String tenantId) {
        if (!aliyunProperties.isSyncSubdomain()) {
            return;
        }

        tenantRepository.cachedByIdOptional(tenantId).ifPresent(tenant -> {
            String subdomainPrefix = tenant.getSubdomainPrefix();
            String subdomainRecordId = tenant.getSubdomainRecordId();

            //新增
            if (isNotBlank(subdomainPrefix) && isBlank(subdomainRecordId)) {
                String recordId = mryDnsService.addCname(subdomainPrefix);
                tenant.onAliyunSubdomainUpdated(recordId);
                tenantRepository.save(tenant);
                log.info("Added CNAME[{}] for tenant[{}] with record ID[{}].", subdomainPrefix, tenantId, recordId);
                return;
            }

            //更新
            if (isNotBlank(subdomainPrefix) && isNotBlank(subdomainRecordId)) {
                String recordId = mryDnsService.updateCname(subdomainRecordId, subdomainPrefix);
                tenant.onAliyunSubdomainUpdated(recordId);
                tenantRepository.save(tenant);
                log.info("Updated CNAME[{}] for tenant[{}] with record ID[{}].", subdomainPrefix, tenantId, recordId);
                return;
            }

            //删除
            if (isBlank(subdomainPrefix) && isNotBlank(subdomainRecordId)) {
                mryDnsService.deleteCname(subdomainRecordId);
                tenant.onAliyunSubdomainDeleted();
                log.info("Deleted CNAME[{}] for tenant[{}] with record ID[{}].", subdomainPrefix, tenantId, subdomainRecordId);
                tenantRepository.save(tenant);
            }
        });
    }
}
