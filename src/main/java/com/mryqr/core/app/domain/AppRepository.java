package com.mryqr.core.app.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AppRepository {
    Set<String> allAppIdsOf(String tenantId);

    App cachedById(String appId);

    App cachedByIdAndCheckTenantShip(String appId, User user);

    Optional<App> cachedByIdOptional(String appId);

    List<TenantCachedApp> cachedTenantAllApps(String tenantId);

    boolean cachedExistsByName(String name, String tenantId);

    App byId(String id);

    Optional<App> byIdOptional(String id);

    App byIdAndCheckTenantShip(String id, User user);

    boolean exists(String arId);

    void save(App app);

    void delete(App app);

    void evictAppCache(String appId);

    void evictTenantAppsCache(String tenantId);

    int countApp(String tenantId);

    List<App> appsOfRange(AttributeStatisticRange range, String startId, int batchSize);

    int removeManagerFromAllApps(String memberId, String tenantId);

    int appTemplateAppliedCountFor(String appTemplateId);

}
