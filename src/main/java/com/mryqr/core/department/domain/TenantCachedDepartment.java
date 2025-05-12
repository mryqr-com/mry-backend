package com.mryqr.core.department.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class TenantCachedDepartment {
    private final String id;
    private final String name;
    private final List<String> managers;
    private String customId;
}
