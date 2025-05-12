package com.mryqr.core.app.domain.report.number;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class NumberReportConfiguration {
    @Min(3)
    @Max(8)
    private int reportPerLine;//每行报告数量

    @Min(0)
    @Max(50)
    private int gutter;//各项报告间隙

    @Min(50)
    @Max(200)
    private int height;//单个report的高度
}
