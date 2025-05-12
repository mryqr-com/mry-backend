package com.mryqr.core.tenant.query;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.order.domain.delivery.Consignee;
import com.mryqr.core.plan.domain.Plan;
import com.mryqr.core.tenant.domain.Packages;
import com.mryqr.core.tenant.domain.ResourceUsage;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.Math.max;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;

@Component
@RequiredArgsConstructor
public class TenantQueryService {
    private final TenantRepository tenantRepository;
    private final MryRateLimiter mryRateLimiter;

    public QTenantInfo fetchTenantInfo(User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:FetchInfo", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());

        Packages packages = tenant.getPackages();
        ResourceUsage resourceUsage = tenant.getResourceUsage();
        Plan currentPlan = tenant.currentPlan();

        return QTenantInfo.builder()
                .tenantId(tenant.getId())
                .name(tenant.getName())
                .planType(tenant.currentPlanType())
                .createdAt(tenant.getCreatedAt())
                .createdBy(tenant.getCreatedBy())
                .creator(tenant.getCreator())
                .packagesName(currentPlan.name())
                .planMaxAppCount(currentPlan.getMaxAppCount())
                .planMaxMemberCount(currentPlan.getMaxMemberCount())
                .planMaxStorage(currentPlan.getMaxStorage())
                .isPackagesExpired(tenant.isPackagesExpired())
                .packagesExpireAt(tenant.packagesExpiredAt())
                .extraMemberCount(packages.getExtraMemberCount())
                .extraStorage(packages.getExtraStorage())
                .extraRemainSmsCount(packages.getExtraRemainSmsCount())
                .usedAppCount(resourceUsage.getAppCount())
                .effectiveMaxAppCount(max(packages.effectiveMaxAppCount(), 0))
                .usedMemberCount(resourceUsage.getMemberCount())
                .effectiveMaxMemberCount(max(packages.effectiveMaxMemberCount(), 0))
                .usedSubmissionCount(resourceUsage.allSubmissionCount())
                .effectiveMaxSubmissionCount(max(packages.effectiveMaxSubmissionCount(), 0))
                .usedQrCount(resourceUsage.allQrCount())
                .effectiveMaxQrCount(max(packages.effectiveMaxQrCount(), 0))
                .usedStorage(valueOf(resourceUsage.getStorage()).setScale(2, HALF_UP).toString())
                .effectiveMaxStorage(valueOf(max(packages.effectiveMaxStorage(), 0)).setScale(2, HALF_UP).toString())
                .usedSmsCountForCurrentMonth(resourceUsage.getSmsSentCountForCurrentMonth())
                .effectiveMaxSmsCountPerMonth(max(packages.effectiveMaxSmsCountPerMonth(), 0))
                .effectiveMaxGroupCountPerApp(max(packages.effectiveMaxGroupCountPerApp(), 0))
                .effectiveMaxDepartmentCount(max(packages.effectiveMaxDepartmentCount(), 0))
                .build();
    }

    public QTenantBaseSetting fetchTenantBaseSetting(User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:FetchBaseSetting", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        return QTenantBaseSetting.builder()
                .id(tenant.getId())
                .name(tenant.getName())
                .loginBackground(tenant.getLoginBackground())
                .build();
    }

    public QTenantLogo fetchTenantLogo(User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:FetchLogo", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        return QTenantLogo.builder()
                .logo(tenant.getLogo())
                .build();
    }

    public QTenantSubdomain fetchTenantSubdomain(User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:FetchSubdomain", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        return QTenantSubdomain.builder()
                .subdomainPrefix(tenant.getSubdomainPrefix())
                .updatable(tenant.subdomainUpdatable())
                .build();
    }

    public QTenantApiSetting fetchTenantApiSetting(User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:FetchApiSetting", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        return QTenantApiSetting.builder()
                .apiSetting(tenant.getApiSetting())
                .build();
    }

    public QTenantPublicProfile fetchTenantPublicProfile(String subdomainPrefix) {
        mryRateLimiter.applyFor("Tenant:FetchPublicProfile:" + subdomainPrefix, 100);

        Tenant tenant = tenantRepository.bySubdomainPrefix(subdomainPrefix);
        return QTenantPublicProfile.builder()
                .tenantId(tenant.getId())
                .name(tenant.getName())
                .logo(tenant.getLogo())
                .loginBackground(tenant.getLoginBackground())
                .build();
    }

    public QTenantInvoiceTitle fetchTenantInvoiceTitle(User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:FetchInvoiceTitle", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        return QTenantInvoiceTitle.builder()
                .title(tenant.getInvoiceTitle())
                .build();
    }

    public List<Consignee> listConsignees(User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Tenant:ListConsignees", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        return tenant.getConsignees();
    }
}
