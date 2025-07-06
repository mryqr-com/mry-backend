package com.mryqr.core.register.command;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.register.domain.RegisterDomainService;
import com.mryqr.core.tenant.domain.CreateTenantResult;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import com.mryqr.core.verification.domain.VerificationCodeChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.mryqr.common.domain.user.Role.TENANT_ADMIN;
import static com.mryqr.core.member.domain.Member.newMemberId;
import static com.mryqr.core.tenant.domain.Tenant.newTenantId;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterCommandService {
    private final RegisterDomainService registerDomainService;
    private final VerificationCodeChecker verificationCodeChecker;
    private final MemberRepository memberRepository;
    private final TenantRepository tenantRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final MryRateLimiter mryRateLimiter;

    @Transactional
    public RegisterResponse register(RegisterCommand command) {
        mryRateLimiter.applyFor("Registration:Register:All", 20);

        User user = User.humanUser(newMemberId(), command.getUsername(), newTenantId(), TENANT_ADMIN);
        CreateTenantResult result = registerDomainService.register(
                command.getMobileOrEmail(),
                command.getPassword(),
                command.getTenantName(),
                user);

        Tenant tenant = result.getTenant();
        Member member = result.getMember();
        DepartmentHierarchy departmentHierarchy = result.getDepartmentHierarchy();

        tenantRepository.save(tenant);
        memberRepository.save(member);
        departmentHierarchyRepository.save(departmentHierarchy);
        log.info("Registered tenant[{}] with admin member[{}].", tenant.getId(), member.getId());

        return RegisterResponse.builder().tenantId(tenant.getId()).memberId(member.getId()).build();
    }
}
