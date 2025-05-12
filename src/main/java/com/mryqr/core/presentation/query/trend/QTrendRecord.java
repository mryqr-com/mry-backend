package com.mryqr.core.presentation.query.trend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QTrendRecord {
    private final String date;
    private final Double number;
}
