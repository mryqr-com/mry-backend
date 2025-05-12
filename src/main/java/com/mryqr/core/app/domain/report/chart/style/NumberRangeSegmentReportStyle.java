package com.mryqr.core.app.domain.report.chart.style;

import com.mryqr.core.common.validation.color.Color;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class NumberRangeSegmentReportStyle {

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String xTitle;

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String yTitle;

    @Color
    private final String color;

    private final boolean hideGrid;

    private final boolean showNumber;

}
