package com.mryqr.core.member.query.profile;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.properties.CommonProperties;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.Packages;
import com.mryqr.core.tenant.domain.PackagesStatus;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import com.mryqr.core.tenant.query.QConsoleTenantProfile;
import com.mryqr.core.tenant.query.QPackagesStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class MemberProfileQueryService {
    private final MemberRepository memberRepository;
    private final TenantRepository tenantRepository;
    private final MongoTemplate mongoTemplate;
    private final MryRateLimiter mryRateLimiter;
    private final CommonProperties commonProperties;

    public QConsoleMemberProfile fetchMyProfile(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:FetchMyProfile", 100);

        String memberId = user.getMemberId();
        String tenantId = user.getTenantId();

        Member member = memberRepository.cachedById(memberId);
        Tenant tenant = tenantRepository.byId(tenantId);

        boolean hasManagedApps;
        if (user.isTenantAdmin()) {
            hasManagedApps = true;
        } else {
            Query query = query(where("tenantId").is(user.getTenantId()).and("managers").is(memberId));
            hasManagedApps = mongoTemplate.exists(query, App.class);
        }

        Packages packages = tenant.getPackages();
        PackagesStatus packagesStatus = tenant.packagesStatus();

        QPackagesStatus planProfile = QPackagesStatus.builder()
                .planName(packages.currentPlan().name())
                .planType(packages.currentPlanType())
                .effectivePlanName(packages.effectivePlan().name())
                .effectivePlanType(packages.effectivePlanType())
                .maxAppReached(packagesStatus.isMaxAppReached())
                .maxMemberReached(packagesStatus.isMaxMemberReached())
                .submissionNotifyAllowed(packages.submissionNotifyAllowed())
                .batchImportQrAllowed(packages.batchImportQrAllowed())
                .batchImportMemberAllowed(packages.batchImportMemberAllowed())
                .submissionApprovalAllowed(packages.submissionApprovalAllowed())
                .reportingAllowed(packages.reportingAllowed())
                .customSubdomainAllowed(packages.customSubdomainAllowed())
                .developerAllowed(packages.developerAllowed())
                .customLogoAllowed(packages.customLogoAllowed())
                .videoAudioAllowed(packages.videoAudioAllowed())
                .assignmentAllowed(packages.assignmentAllowed())
                .supportedControlTypes(packages.effectiveSupportedControlTypes())
                .expired(packages.isExpired())
                .expireAt(packages.expireAt())
                .build();

        QConsoleTenantProfile tenantProfile = QConsoleTenantProfile.builder()
                .tenantId(tenantId)
                .name(tenant.getName())
                .subdomainPrefix(tenant.getSubdomainPrefix())
                .baseDomainName(commonProperties.getBaseDomainName())
                .subdomainReady(tenant.isSubdomainReady())
                .logo(tenant.getLogo())
                .packagesStatus(planProfile)
                .build();

        return QConsoleMemberProfile.builder()
                .memberId(memberId)
                .tenantId(member.getTenantId())
                .name(member.getName())
                .role(member.getRole())
                .avatar(member.getAvatar())
                .hasManagedApps(hasManagedApps)
                .tenantProfile(tenantProfile)
                .topAppIds(member.toppedAppIds())
                .mobileIdentified(member.isMobileIdentified())
                .build();
    }

    public QClientMemberProfile fetchMyClientMemberProfile(User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "Member:FetchMyClientProfile", 100);

        String memberId = user.getMemberId();
        String tenantId = user.getTenantId();

        Member member = memberRepository.cachedById(memberId);
        Tenant tenant = tenantRepository.cachedById(tenantId);

        return QClientMemberProfile.builder()
                .memberId(memberId)
                .memberName(member.getName())
                .avatar(member.getAvatar())
                .tenantId(tenantId)
                .tenantName(tenant.getName())
                .tenantLogo(tenant.getLogo())
                .subdomainPrefix(tenant.getSubdomainPrefix())
                .subdomainReady(tenant.isSubdomainReady())
                .topAppIds(member.toppedAppIds())
                .hideBottomMryLogo(tenant.getPackages().hideBottomMryLogo())
                .reportingAllowed(tenant.getPackages().reportingAllowed())
                .kanbanAllowed(tenant.getPackages().kanbanAllowed())
                .assignmentAllowed(tenant.getPackages().assignmentAllowed())
                .build();
    }

}
