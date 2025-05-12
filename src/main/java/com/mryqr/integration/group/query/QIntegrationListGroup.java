package com.mryqr.integration.group.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QIntegrationListGroup {
    private final String id;
    private final String name;
    private final String appId;
    private final String customId;
    private final boolean archived;
    private final boolean active;
}
