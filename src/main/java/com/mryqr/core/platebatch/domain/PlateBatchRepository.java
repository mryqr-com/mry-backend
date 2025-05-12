package com.mryqr.core.platebatch.domain;

import com.mryqr.common.domain.user.User;

import java.util.Optional;

public interface PlateBatchRepository {
    boolean existsByName(String name, String appId);

    void save(PlateBatch it);

    void delete(PlateBatch it);

    PlateBatch byId(String id);

    Optional<PlateBatch> byIdOptional(String id);

    PlateBatch byIdAndCheckTenantShip(String id, User user);

    int count(String tenantId);

    int deltaCountUsedPlatesForPlateBatch(String plateBatchId, int delta);

    int removeAllPlateBatchUnderApp(String appId);

}
