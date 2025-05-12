package com.mryqr.core.tenant.query;

import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QConsoleTenantProfile {
    private String tenantId;
    private String name;
    private UploadedFile logo;
    private String subdomainPrefix;
    private String baseDomainName;
    private boolean subdomainReady;
    private QPackagesStatus packagesStatus;
}
