package com.mryqr.core.app.domain;

import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.permission.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class TenantCachedApp {
    private String id;
    private String name;
    private UploadedFile icon;
    private List<String> managers;
    private Permission permission;
    private Permission operationPermission;
    private boolean active;
    private boolean groupSynced;
    private boolean locked;
}
