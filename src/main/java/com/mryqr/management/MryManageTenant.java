package com.mryqr.management;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchyRepository;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.tenant.domain.CreateTenantResult;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantFactory;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.mryqr.core.common.domain.user.Role.TENANT_ADMIN;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static java.time.ZoneId.systemDefault;

@Slf4j
@Component
@RequiredArgsConstructor
public class MryManageTenant {
    public static final String MRY_MANAGE_TENANT_ID = "TNT000000000000000001";
    public static final String ADMIN_MEMBER_ID = "MBR000000000000000001";
    public static final String ADMIN_MEMBER_NAME = "码老板";
    public static final String ADMIN_INIT_MOBILE = "15111111111";
    public static final String ADMIN_INIT_PASSWORD = "11111111";
    public static final User MRY_MANAGE_ROBOT_USER = User.robotUser(MRY_MANAGE_TENANT_ID);
    private final TenantRepository tenantRepository;
    private final MemberRepository memberRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final TenantFactory tenantFactory;

    @Transactional
    public void init() {
        if (tenantRepository.exists(MRY_MANAGE_TENANT_ID)) {
            return;
        }

        User user = User.humanUser(ADMIN_MEMBER_ID, ADMIN_MEMBER_NAME, MRY_MANAGE_TENANT_ID, TENANT_ADMIN);
        CreateTenantResult result = tenantFactory.create("码如云自运营", ADMIN_INIT_MOBILE, null, ADMIN_INIT_PASSWORD, user);
        memberRepository.save(result.getMember());
        Tenant tenant = result.getTenant();
        tenant.updatePlanType(FLAGSHIP, LocalDate.of(2099, 12, 31).atStartOfDay(systemDefault()).toInstant(), user);
        tenantRepository.save(tenant);
        departmentHierarchyRepository.save(result.getDepartmentHierarchy());

        log.info("Created mry manage tenant.");
    }
}
