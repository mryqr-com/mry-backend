package com.mryqr.common.domain.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class TimeRange {
    private final Instant startAt;
    private final Instant endAt;

    public static TimeRange of(Instant startAt, Instant endAt) {
        return TimeRange.builder().startAt(startAt).endAt(endAt).build();
    }
}
