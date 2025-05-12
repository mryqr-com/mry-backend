package com.mryqr.core.platetemplate.domain;

import com.mryqr.core.common.domain.user.User;

import java.util.Optional;

public interface PlateTemplateRepository {
    void save(PlateTemplate it);

    void delete(PlateTemplate it);

    PlateTemplate byId(String id);

    Optional<PlateTemplate> byIdOptional(String id);

    PlateTemplate byIdAndCheckTenantShip(String id, User user);

}
