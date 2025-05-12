package com.mryqr.core.platebatch.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QManagedListPlateBatch {
    private final String id;
    private final String name;
    private final int totalCount;
    private final int usedCount;
    private final Instant createdAt;
    private final String createdBy;
    private final String creator;
}
