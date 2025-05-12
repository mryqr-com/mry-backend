package com.mryqr.core.tenant.query;

import com.mryqr.core.tenant.domain.ApiSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QTenantApiSetting {
    private final ApiSetting apiSetting;
}
