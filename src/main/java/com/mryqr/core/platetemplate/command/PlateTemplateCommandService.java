package com.mryqr.core.platetemplate.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.platetemplate.domain.PlateTemplate;
import com.mryqr.core.platetemplate.domain.PlateTemplateFactory;
import com.mryqr.core.platetemplate.domain.PlateTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.mryqr.common.exception.MryException.accessDeniedException;
import static com.mryqr.management.apptemplate.MryAppTemplateTenant.MRY_APP_TEMPLATE_TENANT_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlateTemplateCommandService {
    private final AppRepository appRepository;
    private final PlateTemplateFactory plateTemplateFactory;
    private final PlateTemplateRepository plateTemplateRepository;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public String createPlateTemplate(CreatePlateTemplateCommand command, User user) {
        checkTenant(user);
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "PlateTemplate:Create", 5);

        App app = appRepository.cachedById(command.getAppId());
        PlateTemplate plateTemplate = plateTemplateFactory.create(app, command.getPlateSetting(), user);
        plateTemplateRepository.save(plateTemplate);
        log.info("Created plate template[{}].", plateTemplate.getId());

        return plateTemplate.getId();
    }

    @Transactional
    public void updatePlateTemplate(String plateTemplateId, UpdatePlateTemplateCommand command, User user) {
        checkTenant(user);
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "PlateTemplate:Update", 5);

        PlateTemplate plateTemplate = plateTemplateRepository.byIdAndCheckTenantShip(plateTemplateId, user);
        plateTemplate.update(command.getImage(), command.getOrder(), user);
        plateTemplateRepository.save(plateTemplate);
        log.info("Updated plate template[{}].", plateTemplateId);
    }

    @Transactional
    public void deletePlateTemplate(String plateTemplateId, User user) {
        checkTenant(user);
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "PlateTemplate:Delete", 5);

        PlateTemplate plateTemplate = plateTemplateRepository.byIdAndCheckTenantShip(plateTemplateId, user);
        plateTemplateRepository.delete(plateTemplate);
        log.info("Deleted plate template[{}].", plateTemplateId);
    }

    private void checkTenant(User user) {
        if (!Objects.equals(user.getTenantId(), MRY_APP_TEMPLATE_TENANT_ID)) {
            throw accessDeniedException();
        }
    }
}
