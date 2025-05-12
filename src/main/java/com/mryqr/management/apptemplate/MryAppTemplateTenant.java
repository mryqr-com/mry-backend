package com.mryqr.management.apptemplate;

import com.mryqr.common.domain.user.User;
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

import static com.mryqr.common.domain.user.Role.TENANT_ADMIN;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static java.time.ZoneId.systemDefault;

@Slf4j
@Component
@RequiredArgsConstructor
public class MryAppTemplateTenant {
    public static final String MRY_APP_TEMPLATE_TENANT_ID = "TNT000000000000000002";
    public static final String ADMIN_MEMBER_ID = "MBR000000000000000002";
    public static final String ADMIN_MEMBER_NAME = "码小云";
    public static final String ADMIN_INIT_MOBILE = "15222222222";
    public static final String ADMIN_INIT_PASSWORD = "11111111";
    public static final User APP_TEMPLATE_ROBOT_USER = User.robotUser(MRY_APP_TEMPLATE_TENANT_ID);
    private final TenantRepository tenantRepository;
    private final MemberRepository memberRepository;
    private final DepartmentHierarchyRepository departmentHierarchyRepository;
    private final TenantFactory tenantFactory;

    @Transactional
    public void init() {
        if (tenantRepository.exists(MRY_APP_TEMPLATE_TENANT_ID)) {
            return;
        }

        User user = User.humanUser(ADMIN_MEMBER_ID, ADMIN_MEMBER_NAME, MRY_APP_TEMPLATE_TENANT_ID, TENANT_ADMIN);
        CreateTenantResult result = tenantFactory.create("码如云应用模板管理", ADMIN_INIT_MOBILE, null, ADMIN_INIT_PASSWORD, user);
        memberRepository.save(result.getMember());
        Tenant tenant = result.getTenant();
        tenant.updatePlanType(FLAGSHIP, LocalDate.of(2099, 12, 31).atStartOfDay(systemDefault()).toInstant(), user);
        tenantRepository.save(tenant);
        departmentHierarchyRepository.save(result.getDepartmentHierarchy());

        log.info("Created mry app template manage tenant.");
    }

}
