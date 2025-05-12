package com.mryqr.integration.department.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QIntegrationListDepartment {
    private final String id;
    private final String name;
    private String customId;
}
