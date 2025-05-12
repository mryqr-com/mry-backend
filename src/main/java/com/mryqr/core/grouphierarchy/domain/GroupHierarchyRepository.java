package com.mryqr.core.grouphierarchy.domain;

import com.mryqr.core.common.domain.user.User;

public interface GroupHierarchyRepository {
    GroupHierarchy byAppId(String appId);

    GroupHierarchy byAppIdAndCheckTenantShip(String appId, User user);

    GroupHierarchy cachedByAppId(String appId);

    int count(String tenantId);

    void save(GroupHierarchy groupHierarchy);

    void evictGroupHierarchyCache(String appId);

    int removeGroupHierarchyUnderApp(String appId);
}
