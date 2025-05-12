package com.mryqr.core.appmanual.domain;

import java.util.Optional;

public interface AppManualRepository {
    Optional<AppManual> byAppIdOptional(String appId);

    void save(AppManual it);

    int removeAppManual(String appId);

}
