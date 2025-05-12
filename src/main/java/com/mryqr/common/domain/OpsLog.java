package com.mryqr.common.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class OpsLog {
    private final Instant optAt;
    private final String optBy;
    private final String obn;//operatedByName
    private final String note;

    public Instant getOperatedAt() {
        return optAt;
    }

    public String getOperatedBy() {
        return optBy;
    }

    public String getOperatedByName() {
        return obn;
    }
}
