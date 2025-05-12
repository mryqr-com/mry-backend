package com.mryqr.core.department.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class TenantCachedDepartments {
    private final List<TenantCachedDepartment> departments;
}
