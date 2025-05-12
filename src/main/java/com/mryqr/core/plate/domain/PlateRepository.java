package com.mryqr.core.plate.domain;

import com.mryqr.core.common.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PlateRepository {
    Optional<Plate> byQrIdOptional(String qrId);

    Set<String> allPlateBatchIdsReferencingGroup(String groupId);

    List<String> allPlateIdsUnderPlateBatch(String plateBatchId);

    void save(Plate it);

    void insert(List<Plate> plates);

    Plate byId(String id);

    Optional<Plate> byIdOptional(String id);

    Plate byIdAndCheckTenantShip(String id, User user);

    int count(String tenantId);

    int countPlateUnderTenant(String tenantId);

    int removeAllPlatesUnderApp(String appId);

    int unbindAllPlatesUnderGroup(String groupId);

    int unsetAllPlatesFromPlateBatch(String plateBatchId);

    int countUsedPlatesForPlateBatch(String plateBatchId);

}
