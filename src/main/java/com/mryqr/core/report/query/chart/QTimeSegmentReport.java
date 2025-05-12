package com.mryqr.core.report.query.chart;

import com.mryqr.core.common.domain.report.TimeSegment;
import com.mryqr.core.common.domain.report.TimeSegmentInterval;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.report.query.chart.QChartReportType.TIME_SEGMENT;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QTimeSegmentReport extends QChartReport {
    private TimeSegmentInterval interval;
    private List<List<TimeSegment>> segmentsData;

    public QTimeSegmentReport(TimeSegmentInterval interval, List<List<TimeSegment>> segmentsData) {
        super(TIME_SEGMENT, isNotEmpty(segmentsData));
        this.interval = interval;
        this.segmentsData = segmentsData;
    }

}
