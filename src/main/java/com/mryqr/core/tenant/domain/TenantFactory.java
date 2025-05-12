package com.mryqr.core.tenant.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.password.MryPasswordEncoder;
import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantFactory {
    private final MryPasswordEncoder mryPasswordEncoder;

    public CreateTenantResult create(String tenantName, String mobile, String email, String password, User user) {
        Tenant tenant = new Tenant(tenantName, user);
        DepartmentHierarchy departmentHierarchy = new DepartmentHierarchy(user);
        Member member = new Member(mobile, email, mryPasswordEncoder.encode(password), user);
        return CreateTenantResult.builder()
                .tenant(tenant)
                .member(member)
                .departmentHierarchy(departmentHierarchy)
                .build();
    }
}
