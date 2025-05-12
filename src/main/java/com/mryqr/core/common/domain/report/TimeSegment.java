package com.mryqr.core.common.domain.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class TimeSegment {
    private final int year;
    private final int period;
    private final Double value;
}
