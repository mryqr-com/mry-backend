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

import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class TimeSegmentReportStyle {

    @Min(1)
    @Max(20)
    private final int max;

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String xTitle;

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String yTitle;

    @Valid
    @NotNull
    @Size(max = 2)
    private List<@Color String> colors;

    private final boolean hideGrid;

    private final boolean showNumber;

    private final boolean horizontal;

}
