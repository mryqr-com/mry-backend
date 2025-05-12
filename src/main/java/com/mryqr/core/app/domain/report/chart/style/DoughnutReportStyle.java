package com.mryqr.core.app.domain.report.chart.style;

import com.mryqr.common.validation.color.Color;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class DoughnutReportStyle {

    @Min(1)
    @Max(20)
    private final int max;

    private boolean showValue;

    private boolean showPercentage;

    private boolean showLabels;

    private boolean showCenterTotal;

    @Valid
    @NotNull
    @Size(max = 20)
    private List<@Color String> colors;//区域颜色

}
