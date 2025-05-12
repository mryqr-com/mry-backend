package com.mryqr.core.presentation.query.trend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QTrendDataSet {
    private final String label;
    private final List<QTrendRecord> records;
}
