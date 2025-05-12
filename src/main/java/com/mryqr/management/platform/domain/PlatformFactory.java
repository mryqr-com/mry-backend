package com.mryqr.management.platform.domain;

import org.springframework.stereotype.Component;

import static com.mryqr.common.domain.user.User.robotUser;
import static com.mryqr.management.MryManageTenant.MRY_MANAGE_TENANT_ID;

@Component
public class PlatformFactory {
    public Platform createPlatform() {
        return new Platform(robotUser(MRY_MANAGE_TENANT_ID));
    }
}
