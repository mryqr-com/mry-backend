package com.mryqr.core.report.query.chart;

import com.mryqr.common.domain.stat.NumberRangeSegment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.report.query.chart.QChartReportType.NUMBER_RANGE_SEGMENT;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QNumberRangeSegmentReport extends QChartReport {
    private List<Double> numberRanges;
    private List<NumberRangeSegment> segments;

    public QNumberRangeSegmentReport(List<Double> numberRanges, List<NumberRangeSegment> segments) {
        super(NUMBER_RANGE_SEGMENT, isNotEmpty(segments));
        this.numberRanges = numberRanges;
        this.segments = segments;
    }
}
