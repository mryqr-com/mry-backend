package com.mryqr.integration.app.query;

import com.mryqr.core.common.domain.permission.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QIntegrationListApp {
    private final String id;
    private final String name;
    private final boolean active;
    private final boolean locked;
    private final String version;
    private final Permission permission;
    private final Permission operationPermission;
}
