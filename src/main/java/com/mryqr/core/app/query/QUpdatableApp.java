package com.mryqr.core.app.query;

import com.mryqr.core.app.domain.AppSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QUpdatableApp {
    private String id;
    private String name;
    private String tenantId;
    private String version;
    private boolean webhookEnabled;
    private AppSetting setting;
}
