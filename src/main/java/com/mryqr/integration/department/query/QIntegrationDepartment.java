package com.mryqr.integration.department.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QIntegrationDepartment {
    private final String id;
    private final String name;
    private String customId;
    private List<String> managers;
    private Set<String> members;
    private Instant createdAt;
}
