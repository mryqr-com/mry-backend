package com.mryqr.common.domain.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class CategorizedOptionSegment {
    private final String option;
    private final Double value;
}
