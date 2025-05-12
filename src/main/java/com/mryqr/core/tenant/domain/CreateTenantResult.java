package com.mryqr.core.tenant.domain;

import com.mryqr.core.departmenthierarchy.domain.DepartmentHierarchy;
import com.mryqr.core.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CreateTenantResult {
    private final Tenant tenant;
    private final Member member;
    private final DepartmentHierarchy departmentHierarchy;
}
