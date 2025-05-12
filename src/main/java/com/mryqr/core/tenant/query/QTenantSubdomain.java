package com.mryqr.core.tenant.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QTenantSubdomain {
    private final String subdomainPrefix;
    private final boolean updatable;
}
