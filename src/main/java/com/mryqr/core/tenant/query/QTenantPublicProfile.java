package com.mryqr.core.tenant.query;

import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QTenantPublicProfile {
    private final String tenantId;
    private final String name;
    private final UploadedFile logo;
    private final UploadedFile loginBackground;
}
