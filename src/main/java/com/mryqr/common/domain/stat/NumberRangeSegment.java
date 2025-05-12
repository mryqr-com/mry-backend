package com.mryqr.common.domain.stat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class NumberRangeSegment {
    private final double segment;
    private final Double value;
}
