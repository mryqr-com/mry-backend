package com.mryqr.integration.app.query;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.permission.Permission;
import com.mryqr.core.app.domain.AppSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QIntegrationApp {
    private final String id;
    private final String name;
    private final UploadedFile icon;
    private final boolean active;
    private final boolean locked;
    private final String version;
    private final AppSetting setting;
    private final Permission permission;
    private final Permission operationPermission;
    private final Instant createdAt;
    private final String createdBy;
}
