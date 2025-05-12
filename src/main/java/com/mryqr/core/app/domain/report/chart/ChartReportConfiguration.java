package com.mryqr.core.app.domain.report.chart;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ChartReportConfiguration {

    @Min(0)
    @Max(50)
    private final int gutter;

}
