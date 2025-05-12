package com.mryqr.integration.app.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IntegrationAppCommandService {
    private final AppRepository appRepository;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public void activateApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:App:Activate", 10);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        app.activate(user);
        appRepository.save(app);
        log.info("Integration activated app[{}].", appId);
    }

    @Transactional
    public void deactivateApp(String appId, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Integration:App:Deactivate", 10);

        App app = appRepository.byIdAndCheckTenantShip(appId, user);
        app.deactivate(user);
        appRepository.save(app);
        log.info("Integration deactivated app[{}].", appId);
    }
}
