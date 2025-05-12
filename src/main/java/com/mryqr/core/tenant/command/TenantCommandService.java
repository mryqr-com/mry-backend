package com.mryqr.core.tenant.command;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.plan.domain.Plan;
import com.mryqr.core.plan.domain.PlanType;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantDomainService;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TenantCommandService {
    private final TenantRepository tenantRepository;
    private final TenantDomainService tenantDomainService;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public void updateTenantBaseSetting(UpdateTenantBaseSettingCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:UpdateBaseSetting", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        tenant.updateBaseSetting(command.getName(), command.getLoginBackground(), user);
        tenantRepository.save(tenant);
        log.info("Updated base setting for tenant[{}].", user.getTenantId());
    }

    @Transactional
    public void updateTenantLogo(UpdateTenantLogoCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:UpdateLogo", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        tenant.packagesStatus().validateUpdateLogo();

        tenant.updateLogo(command.getLogo(), user);
        tenantRepository.save(tenant);
        log.info("Updated logo for tenant[{}].", user.getTenantId());
    }

    @Transactional
    public void updateTenantSubdomain(UpdateTenantSubdomainCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:UpdateSubdomain", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        tenant.packagesStatus().validateUpdateSubdomain();

        tenantDomainService.updateSubdomain(tenant, command.getSubdomainPrefix(), user);
        tenantRepository.save(tenant);
        log.info("Updated subdomain for tenant[{}] with prefix[{}].", user.getTenantId(), command.getSubdomainPrefix());

    }

    @Transactional
    public String refreshTenantApiSecret(User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:RefreshApiSecret", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        tenant.packagesStatus().validateRefreshApiSecret();

        tenant.refreshApiSecret(user);
        tenantRepository.save(tenant);
        log.info("Refreshed API secret for tenant[{}].", user.getTenantId());
        return tenant.getApiSetting().getApiSecret();
    }

    @Transactional
    public void updateTenantInvoiceTitle(UpdateTenantInvoiceTitleCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:UpdateInvoiceTitle", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        tenant.updateInvoiceTitle(command.getTitle(), user);
        tenantRepository.save(tenant);
        log.info("Updated invoice title for tenant[{}].", user.getTenantId());
    }

    @Transactional
    public void addConsignee(AddConsigneeCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:AddConsignee", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        tenant.addConsignee(command.getConsignee(), user);
        tenantRepository.save(tenant);
        log.info("Added consignee[{}] for tenant[{}].", command.getConsignee().getId(), user.getTenantId());
    }

    @Transactional
    public void updateConsignee(UpdateConsigneeCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:UpdateConsignee", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        tenant.updateConsignee(command.getConsignee(), user);
        tenantRepository.save(tenant);
        log.info("Updated consignee[{}] for tenant[{}].", command.getConsignee().getId(), user.getTenantId());
    }

    @Transactional
    public void deleteConsignee(String consigneeId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:DeleteConsignee", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        tenant.deleteConsignee(consigneeId, user);
        tenantRepository.save(tenant);
        log.info("Deleted consignee[{}] for tenant[{}].", consigneeId, user.getTenantId());
    }

    @Transactional
    public void updateTenantPlanType(String tenantId, PlanType planType, Instant expire, User user) {
        mryRateLimiter.applyFor(tenantId, "Tenant:UpdatePlanType", 5);

        Tenant tenant = tenantRepository.byId(tenantId);
        tenant.updatePlanType(planType, expire, user);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void updateTenantPlan(String tenantId, Plan plan, User user) {
        mryRateLimiter.applyFor(tenantId, "Tenant:UpdatePlan", 5);

        Tenant tenant = tenantRepository.byId(tenantId);
        tenant.updatePlan(plan, user);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void activateTenant(String tenantId, User user) {
        mryRateLimiter.applyFor(tenantId, "Tenant:Activate", 5);

        Tenant tenant = tenantRepository.byId(tenantId);
        tenant.activate(user);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void deactivateTenant(String tenantId, User user) {
        mryRateLimiter.applyFor(tenantId, "Tenant:Deactivate", 5);

        Tenant tenant = tenantRepository.byId(tenantId);
        tenant.deactivate(user);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void clearSubdomain(String tenantId, User user) {
        mryRateLimiter.applyFor(tenantId, "Tenant:ClearSubdomain", 5);

        Tenant tenant = tenantRepository.byId(tenantId);
        tenant.forceClearSubdomain(user);
        tenantRepository.save(tenant);
    }

    @Transactional
    public void updateSubdomainReadyStatus(String tenantId, boolean ready, User user) {
        mryRateLimiter.applyFor(tenantId, "Tenant:UpdateSubdomainReady", 5);

        Tenant tenant = tenantRepository.byId(tenantId);
        tenant.updateSubdomainReadyStatus(ready, user);
        tenantRepository.save(tenant);
    }
}
