package com.mryqr.integration.group.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QIntegrationGroup {
    private final String id;
    private final String name;
    private final String appId;
    private final String customId;
    private final List<String> managers;
    private final List<String> members;
    private final boolean archived;
    private final boolean active;
    private final Instant createdAt;
    private final String createdBy;
}
