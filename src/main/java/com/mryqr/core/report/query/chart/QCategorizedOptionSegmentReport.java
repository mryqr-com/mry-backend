package com.mryqr.core.report.query.chart;

import com.mryqr.common.domain.stat.CategorizedOptionSegment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.mryqr.core.report.query.chart.QChartReportType.CATEGORIZED_OPTION_SEGMENT;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class QCategorizedOptionSegmentReport extends QChartReport {
    private List<List<CategorizedOptionSegment>> segmentsData;

    public QCategorizedOptionSegmentReport(List<List<CategorizedOptionSegment>> segmentsData) {
        super(CATEGORIZED_OPTION_SEGMENT, isNotEmpty(segmentsData));
        this.segmentsData = segmentsData;
    }
}
