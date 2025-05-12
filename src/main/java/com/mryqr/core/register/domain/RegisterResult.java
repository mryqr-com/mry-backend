package com.mryqr.core.register.domain;

import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.member.domain.Member;
import com.mryqr.core.tenant.domain.Tenant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterResult {
    private final Member member;
    private final Tenant tenant;
    private final DepartmentHierarchy departmentHierarchy;
}
